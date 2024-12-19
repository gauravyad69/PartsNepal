import { Component, CUSTOM_ELEMENTS_SCHEMA, Input, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartItem } from '../../models/cart';
import { WishItem } from '../../models/wishlist';
import { HotToastService } from '@ngneat/hot-toast';
import { ProductModel, CategoryModelReq, CategoryModelRes } from '../../models/product.model';
import { NgOptimizedImage } from '@angular/common';
import { PriceFormatPipe } from '../pipe/price-format.pipe';
import { CategoryService } from '../services/category.service';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss'],
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe,
    NgOptimizedImage,
    PriceFormatPipe
  ]
})
export class ProductComponent implements OnInit {
  @Input() product!: ProductModel;
  isProductInWishList: boolean = false;
  WishItems!: WishItem[];
  categories: CategoryModelRes[] = [];

  constructor(
    private _toast: HotToastService,
    private categoryService: CategoryService
  ) { }

  ngOnInit() {
    this.loadCategories();
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

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/ImageNotFound.png';
  }
}