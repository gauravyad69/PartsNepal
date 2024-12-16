import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';
import { ProductService } from '../services/product.service';
import { HotToastService } from '@ngneat/hot-toast';
import { ProductComponent } from '../product/product.component';
import { FilterPipe } from '../pipe/filter.pipe';
import { FormsModule } from '@angular/forms';
import { ApiResponse } from '../../models/api-response';
import { ProductModel } from '../../models/product.model';
import { PriceFormatPipe } from '../pipe/price-format.pipe';
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
    PriceFormatPipe,
    FormsModule
  ]
})
export class AllProductsComponent implements OnInit {
  Loading: boolean = true;
  products: ProductModel[] = [];
  fliterValue: string = "Default";
  
  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 20;
  totalItems: number = 0;
  totalPages: number = 0;
  Math = Math; // For using Math in template

  constructor(
    private _product: ProductService,
    private _toast: HotToastService
  ) {
    console.log('AllProductsComponent constructed');
  }

  getProducts(page: number) {
    this.Loading = true;
    
    this._product.getProduct(page - 1, this.itemsPerPage).subscribe({
      next: (response: ApiResponse<ProductModel[]>) => {
        console.log('API Response:', response);
        this.products = response.data;
        console.log('Products array:', this.products);
        this.totalItems = response.metadata?.totalItems || 0;
        this.totalPages = response.metadata?.totalPages || 0;
        this.currentPage = (response.metadata?.page || 0) + 1;
        this.itemsPerPage = response.metadata?.itemsPerPage || this.itemsPerPage;
        this.Loading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
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


  ngOnInit(): void {
    this.getProducts(1);
    console.log('AllProductsComponent initialized');
  }
}
