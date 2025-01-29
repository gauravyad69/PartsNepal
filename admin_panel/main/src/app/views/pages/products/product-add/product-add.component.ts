import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductService } from '../services/product.service';
import { HotToastService } from '@ngneat/hot-toast';
import { CategoryModelRes, DeliveryOption, DiscountType, ProductModel } from '../../models/product.model';
import { PriceFormatPipe } from '../pipe/price-format.pipe';
import { CategoryService } from '../services/category.service';
import { CategoryModelReq } from '../../models/product.model';
import { UploadService } from '../services/upload.service';

interface ImageUpload {
  url: string;
  alt: string;
  isPrimary: boolean;
  order: number;
  file?: File;
  preview?: string;
  uploading?: boolean;
  progress?: number;
}

@Component({
  selector: 'app-product-add',
  templateUrl: './product-add.component.html',
  styleUrls: ['./product-add.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PriceFormatPipe
  ]
})
export class ProductAddComponent {
  productForm!: FormGroup;
  deliveryOptions = Object.values(DeliveryOption);
  discountTypes = Object.values(DiscountType);
  isSubmitting = false;
  imageUrls: ImageUpload[] = [{ url: '', alt: '', isPrimary: false, order: 0 }];
  highlights: string[] = [''];
  categories: CategoryModelRes[] = [];
  mainImageUploading = false;
  mainImagePreview: string | null = null;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router,
    private toast: HotToastService,
    private uploadService: UploadService
  ) {
    this.initForm();
    this.loadCategories();
  }

  private generateProductId(): number {
    return Math.floor(10000 + Math.random() * 90000);
  }

  private initForm() {
    const productId = this.generateProductId();
    this.productForm = this.fb.group({
      basic: this.fb.group({
        productId: [productId],
        productSKU: ['', Validators.required],
        productName: ['', Validators.required],
        categoryId: [0, Validators.required],
        inventory: this.fb.group({
          stock: [0, [Validators.required, Validators.min(0)]],
          mainImage: ['', Validators.required],
          isAvailable: [true]
        }),
        pricing: this.fb.group({
          regularPrice: this.fb.group({
            amount: [0, [Validators.required, Validators.min(0)]],
            currency: ['NPR']
          }),
          salePrice: this.fb.group({
            amount: [0],
            currency: ['NPR']
          }),
          discount: this.fb.group({
            amount: this.fb.group({
              amount: [0],
              currency: ['NPR']
            }),
            type: [DiscountType.PERCENTAGE],
            description: [''],
            startDate: [null],
            endDate: [null]
          })
        })
      }),
      details: this.fb.group({
        productId: [productId],
        description: ['', Validators.required],
        addDate: [Date.now()],
        features: this.fb.group({
          highlights: [[]],
          images: [[]],
          reviews: this.fb.group({
            items: [[]],
            summary: this.fb.group({
              averageRating: [0.0],
              totalCount: [0],
              distribution: {}
            })
          })
        }),
        delivery: this.fb.group({
          options: [[DeliveryOption.STANDARD_DELIVERY]],
          estimatedDays: [3, [Validators.required, Validators.min(1)]],
          shippingCost: this.fb.group({
            amount: [0],
            currency: ['NPR']
          })
        }),
        warranty: this.fb.group({
          isReturnable: [true],
          returnPeriodDays: [30],
          warrantyMonths: [12],
          terms: [[
            'Must be unused and in original packaging',
            'Warranty covers manufacturing defects'
          ]]
        })
      })
    });
  }

  addImageUrl() {
    this.imageUrls.push({ 
      url: '', 
      alt: '', 
      isPrimary: false, 
      order: this.imageUrls.length 
    });
  }

  removeImageUrl(index: number) {
    this.imageUrls.splice(index, 1);
    // Update order for remaining images
    this.imageUrls.forEach((img, idx) => img.order = idx);
  }

  addHighlight() {
    this.highlights.push('');
  }

  removeHighlight(index: number) {
    this.highlights.splice(index, 1);
  }

  updateHighlights() {
    const validHighlights = this.highlights.filter(h => h.trim() !== '');
    this.productForm.get('details.features.highlights')?.setValue(validHighlights);
  }

  updateImages() {
    const validImages = this.imageUrls.filter(img => img.url.trim() !== '');
    this.productForm.get('details.features.images')?.setValue(validImages);
  }

  private dateToUnixTimestamp(date: string | null): number | null {
    if (!date) return null;
    return new Date(date).getTime();
  }

  private loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        this.toast.error('Failed to load categories');
      }
    });
  }

  onSubmit() {
    this.updateHighlights();
    this.updateImages();

    if (this.productForm.invalid) {
      this.toast.error('Please fill all required fields correctly');
      return;
    }

    this.isSubmitting = true;
    const formData = this.productForm.value;
    
    // Create the properly structured product data
    const productData:ProductModel = {
      basic: {
        productId: formData.basic.productId,
        productSKU: formData.basic.productSKU,
        productName: formData.basic.productName,
        categoryId: formData.basic.categoryId,
        inventory: {
          stock: formData.basic.inventory.stock,
          mainImage: formData.basic.inventory.mainImage,
          isAvailable: formData.basic.inventory.isAvailable
        },
        pricing: {
          regularPrice: {
            amount: formData.basic.pricing.regularPrice.amount * 100, // Convert to cents
            currency: 'NPR'
          },
          salePrice: formData.basic.pricing.salePrice.amount ? {
            amount: formData.basic.pricing.salePrice.amount * 100,
            currency: 'NPR'
          } : null,
          discount: formData.basic.pricing.discount.amount.amount ? {
            amount: {
              amount: formData.basic.pricing.discount.amount.amount * 100,
              currency: 'NPR'
            },
            type: formData.basic.pricing.discount.type,
            description: formData.basic.pricing.discount.description || null,
            startDate: this.dateToUnixTimestamp(formData.basic.pricing.discount.startDate),
            endDate: this.dateToUnixTimestamp(formData.basic.pricing.discount.endDate)
          } : null,
        }
      },
      details: {
        productId: formData.basic.productId,
        description: formData.details.description,
        // addDate: Date.now(),
        features: {
          highlights: this.highlights.filter(h => h.trim() !== ''),
          images: this.imageUrls.filter(img => img.url.trim() !== '').map((img, index) => ({
            url: img.url,
            alt: img.alt || '',
            isPrimary: img.isPrimary,
            order: index
          })),
          reviews: {
            items: [],
            summary: {
              averageRating: 0.0,
              totalCount: 0,
              distribution: {}
            }
          }
        },
        delivery: {
          options: formData.details.delivery.options || [DeliveryOption.STANDARD_DELIVERY],
          estimatedDays: formData.details.delivery.estimatedDays,
          shippingCost: {
            amount: formData.details.delivery.shippingCost.amount * 100,
            currency: 'NPR'
          }
        },
        warranty: {
          isReturnable: formData.details.warranty.isReturnable,
          returnPeriodDays: formData.details.warranty.returnPeriodDays,
          warrantyMonths: formData.details.warranty.warrantyMonths,
          terms: [
            'Must be unused and in original packaging',
            'Warranty covers manufacturing defects'
          ]
        }
      }
    };

    this.productService.createProduct(productData)
      .subscribe({
        next: (response) => {
          this.toast.success('Product created successfully');
          this.router.navigate(['/products', response.basic.productId]);
        },
        error: (error) => {
          this.toast.error('Failed to create product: ' + error.message);
          this.isSubmitting = false;
        }
      });
  }

  // Main image upload
  async onMainImageSelect(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    if (!this.validateFile(file)) {
      this.toast.error('Invalid file type. Please upload an image file (jpg, png, gif)');
      return;
    }

    // Show preview
    this.mainImagePreview = URL.createObjectURL(file);
    this.mainImageUploading = true;

    try {
      const fileUrl = await this.uploadService.uploadFile(file).toPromise();
      this.productForm.get('basic.inventory.mainImage')?.setValue(fileUrl);
      this.toast.success('Main image uploaded successfully');
    } catch (error) {
      this.toast.error('Failed to upload main image');
      this.mainImagePreview = null;
    } finally {
      this.mainImageUploading = false;
    }
  }

  // Additional images upload
  async onAdditionalImageSelect(event: Event, index: number) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    if (!this.validateFile(file)) {
      this.toast.error('Invalid file type. Please upload an image file (jpg, png, gif)');
      return;
    }

    // Show preview
    this.imageUrls[index].preview = URL.createObjectURL(file);
    this.imageUrls[index].uploading = true;

    try {
      const fileUrl = await this.uploadService.uploadFile(file).toPromise();
      this.imageUrls[index].url = fileUrl || '';
      this.updateImages();
      this.toast.success('Image uploaded successfully');
    } catch (error) {
      this.toast.error('Failed to upload image');
      this.imageUrls[index].preview = undefined;
    } finally {
      this.imageUrls[index].uploading = false;
    }
  }

  private validateFile(file: File): boolean {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    if (!allowedTypes.includes(file.type)) {
      return false;
    }

    if (file.size > maxSize) {
      this.toast.error('File size should be less than 5MB');
      return false;
    }

    return true;
  }
} 