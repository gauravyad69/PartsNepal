import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';
import { CartItem } from '../../models/cart';
import { WishItem } from '../../models/wishlist';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { ProductService } from '../services/product.service';
import { HotToastService } from '@ngneat/hot-toast';
import { ProductComponent } from '../product/product.component';
import { FilterPipe } from '../pipe/filter.pipe';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-all-products',
  templateUrl: './all-products.component.html',
  styleUrls: ['./all-products.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NgxSkeletonLoaderModule,
    ProductComponent,
    FilterPipe,
    FormsModule
  ]
})
export class AllProductsComponent implements OnInit {
  Loading: boolean = true;
  products: any[] = [];
  WishItems!: WishItem[];
  fliterValue: string = "Default";
  
  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 20;
  totalItems: number = 0;
  totalPages: number = 0;
  Math = Math; // For using Math in template

  constructor(
    private _product: ProductService,
    private _cartService: CartService,
    private _wishlistService: WishlistService,
    private _toast: HotToastService
  ) {
    console.log('AllProductsComponent constructed');
  }

  getProducts(page: number) {
    this.Loading = true;
    const offset = (page - 1) * this.itemsPerPage;
    
    this._product.getProduct(offset, this.itemsPerPage).subscribe({
      next: (data) => {
        this.products = data;
        this.totalItems = 178; // Replace with actual total from API
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.Loading = false;
      },
      error: (error) => {
        this._toast.error('Failed to load products');
        this.Loading = false;
      }
    });
  }

  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.getProducts(page);
    }
  }

  onItemsPerPageChange() {
    this.currentPage = 1;
    this.getProducts(1);
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    for (let i = Math.max(1, this.currentPage - 2); 
         i <= Math.min(this.totalPages, this.currentPage + 2); i++) {
      pages.push(i);
    }
    return pages;
  }

  getWishList() {
    this._wishlistService.wishList$.subscribe((cart) => {
      this.WishItems = cart.items!;
    });
  }

  ngOnInit(): void {
    this.getProducts(1);
    this.getWishList();
    console.log('AllProductsComponent initialized');
  }
}
