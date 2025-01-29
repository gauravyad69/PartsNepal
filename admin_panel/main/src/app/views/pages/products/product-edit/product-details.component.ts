import { Component, CUSTOM_ELEMENTS_SCHEMA, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { CarouselModule, OwlOptions } from 'ngx-owl-carousel-o';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../services/product.service';
import { HotToastService } from '@ngneat/hot-toast';
import { CategoryModelRes, ProductModel } from '../../models/product.model';
import { CartItem } from '../../models/cart';
import { WishItem } from '../../models/wishlist';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';
import { get, set, cloneDeep } from 'lodash';
import { CarouselService } from 'ngx-owl-carousel-o/lib/services/carousel.service';
import { PriceFormatPipe } from '../pipe/price-format.pipe';
import { CategoryService } from '../services/category.service';
import { CategoryModelReq } from '../../models/product.model';
import { UploadService } from '../services/upload.service';

@Component({ 
  selector: 'app-product-details',
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.css'],
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    CarouselModule,
    NgxSkeletonLoaderModule,
    FormsModule,
    PriceFormatPipe
  ] 
})
export class ProductDetailsComponent implements OnInit {
  backgroundPos: string = 'center center';
  startPosition: number = 0; // Position of active Slide
  @ViewChild("myCarousel") myCarousel!: ElementRef;  // slider One Big Image

  slider1Settings: OwlOptions = {
    loop: true,
    mouseDrag: false,
    touchDrag: false,
    pullDrag: false,
    dots: false,
    navSpeed: 700,
    // navText: ['', ''],
    responsive: {
      0: {
        items: 1
      },
      400: {
        items: 1
      },
      740: {
        items: 3
      },
      940: {
        items: 4
      }
    },
    nav: false,
    startPosition: this.startPosition
  }

  slider2Settings: OwlOptions = {
    loop: true,
    mouseDrag: true,
    touchDrag: true,
    pullDrag: true,
    margin: 10,
    dots: false,
    navSpeed: 700,
    center: true,
    // navText: ['', ''],
    responsive: {
      0: {
        items: 3
      },
      400: {
        items: 3
      },
      740: {
        items: 3
      },
      940: {
        items: 3
      }
    },
    nav: false,
    // animateOut: 'slideOutUp',
    // animateIn: 'slideInUp'
  }

  slider3Settings: OwlOptions = {
    loop: true,
    mouseDrag: true,
    touchDrag: true,
    pullDrag: true,
    margin: 10,
    dots: false,
    navSpeed: 700,
    navText: ['<i class="fa-solid fa-arrow-left"></i>', '<i class="fa-solid fa-arrow-right"></i>'],
    responsive: {
      0: {
        items: 1
      },
      400: {
        items: 1
      },
      740: {
        items: 3
      },
      940: {
        items: 5
      },
      1200: {
        items: 5
      },
      1400: {
        items: 5
      },
      1600: {
        items: 5
      }
    },
    nav: true,
  }

  product?: ProductModel;
  editedProduct!: ProductModel;
  isEditing: boolean = false;
  productId!: number;
  categoryId!: number
  imgNotFounded: boolean = false;
  cartList!: CartItem[];
  WishItems!: WishItem[];
  quantity!: number
  loremText: string = `Lorem ipsum dolor sit amet consectetur, adipisicing elit. Iusto, quos aspernatur eum dolorr eprehenderit eos et libero debitis itaque voluptatem! Laudantium modi sequi, id numquam liberosed quaerat. Eligendi, ipsum!`;
  categoryProducts: any
  isProductInWishList: boolean = false;
  productInCartList: any;
  isLoading: boolean = true;
  categories: CategoryModelRes[] = [];
  imageUploading: boolean = false;

  constructor(
    private _productService: ProductService,
    private _route: ActivatedRoute,
    private _toast: HotToastService,
    private categoryService: CategoryService,
    private uploadService: UploadService
  ) { }

  ZoomImage(e: MouseEvent) {
    const zoomer = e.currentTarget as HTMLElement;
    const offsetX = e.offsetX;
    const offsetY = e.offsetY;
    const x = (offsetX / zoomer.offsetWidth) * 100;
    const y = (offsetY / zoomer.offsetHeight) * 100;
    this.backgroundPos = `${x}% ${y}%`;
  }

  nextSlide(event: any) {
    if (event.dragging == false) {
      this.startPosition = event.data.startPosition;
      const anyService = this.myCarousel as any;
      const carouselService = anyService.carouselService as CarouselService;
      carouselService.to(this.startPosition, 3)
    }
  }

  getProductsByCategory(categoryId: number) {
    this._productService.getProductsByCategory(categoryId).subscribe((data) => {
      this.categoryProducts = data;
    })
  }

  startEditing() {
    this.editedProduct = this.product ? cloneDeep(this.product) : {} as ProductModel;
    this.isEditing = true;
  }

  cancelEditing() {
    this.editedProduct = {} as ProductModel;
    this.isEditing = false;
  }

  updateField(path: string, value: any) {
    if (!this.editedProduct) return;
    set(this.editedProduct, path, value);
  }

  saveChanges() {
    if (!this.editedProduct || !this.product) return;

    const updates: Promise<any>[] = [];

    // Check if basic info changed
    if (JSON.stringify(this.product.basic) !== JSON.stringify(this.editedProduct.basic)) {
      updates.push(
        this._productService.updateBasicInfo(this.productId, this.editedProduct.basic).toPromise()
      );
    }

    // Check if inventory changed
    if (this.product.basic.inventory.stock !== this.editedProduct.basic.inventory.stock) {
      updates.push(
        this._productService.updateInventory(this.productId, this.editedProduct.basic.inventory.stock).toPromise()
      );
    }

    // Check if pricing changed
    if (JSON.stringify(this.product.basic.pricing) !== JSON.stringify(this.editedProduct.basic.pricing)) {
      updates.push(
        this._productService.updatePricing(this.productId, this.editedProduct.basic.pricing).toPromise()
      );
    }

    // Check if details changed
    if (JSON.stringify(this.product.details) !== JSON.stringify(this.editedProduct.details)) {
      updates.push(
        this._productService.updateDetailedInfo(this.productId, this.editedProduct.details).toPromise()
      );
    }

    Promise.all(updates)
      .then(() => {
        this._toast.success('Product updated successfully');
        this.product = cloneDeep(this.editedProduct);
        this.isEditing = false;
      })
      .catch(error => {
        this._toast.error('Failed to update product');
        console.error('Update error:', error);
      });
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/ImageNotFound.png';
  }

  ngOnInit(): void {
    this.loadCategories();
    this._route.params.subscribe((params: any) => {
      const id = params['id'];
      if (id) {
        this.productId = id;
        this._productService.getSingleProduct(id).subscribe({
          next: (data) => {
            this.product = data;
          },
          error: (error) => {
            this._toast.error('Failed to load product');
            console.error('Load error:', error);
          }
        });
      }
    });
  }

  private loadCategories() {
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        this._toast.error('Failed to load categories');
      }
    });
  }

  getCategoryName(categoryId: string): string {
    const category = this.categories.find(c => c.categoryId === categoryId);
    if (category) {
      return `${category.categoryName}${category.subCategoryName ? ' - ' + category.subCategoryName : ''}`;
    }
    return 'Unknown Category';
  }

  async onImageUpload(event: Event, index: number) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    if (!this.validateFile(file)) {
      this._toast.error('Invalid file type. Please upload an image file (jpg, png, gif)');
      return;
    }

    this.imageUploading = true;
    
    try {
      this._toast.info('Compressing and uploading image...');
      const fileUrl = await this.uploadService.uploadFile(file);
      
      if (!this.editedProduct) {
        this.editedProduct = this.product ? cloneDeep(this.product) : {} as ProductModel;
      }

      // Update the image URL in the images array
      if (!this.editedProduct?.details?.features?.images) {
        this.editedProduct.details.features.images = [];
      }
      
      if (index === -1) {
        // Adding new image
        this.editedProduct.details.features.images.push({
          url: fileUrl || '',
          alt: file.name,
          isPrimary: false,
          order: this.editedProduct.details.features.images.length
        });
      } else {
        // Updating existing image
        this.editedProduct.details.features.images[index].url = fileUrl || '';
      }

      this._toast.success('Image uploaded successfully');
      this.isEditing = true; // Enable editing mode to show save changes button
    } catch (error) {
      this._toast.error('Failed to upload image');
      console.error('Upload error:', error);
    } finally {
      this.imageUploading = false;
    }
  }

  validateFile(file: File): boolean {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    if (!allowedTypes.includes(file.type)) {
      this._toast.error('Invalid file type. Please upload a JPG, PNG, or GIF file');
      return false;
    }

    if (file.size > maxSize) {
      this._toast.error('File size should be less than 5MB');
      return false;
    }

    return true;
  }

  removeImage(index: number) {
    if (!this.editedProduct?.details?.features?.images) return;
    
    this.editedProduct.details.features.images.splice(index, 1);
    this.isEditing = true; // Enable editing mode to show save changes button
  }

  getAllProductImages() {
    const mainImages = this.editedProduct?.details?.features.images ?? this.product?.details?.features?.images ?? [];
    const featureImages = this.editedProduct?.details?.features?.images ?? this.product?.details?.features?.images ?? [];
    return [...mainImages, ...featureImages];
  }
}
 