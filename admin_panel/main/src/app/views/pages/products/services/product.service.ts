import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../../models/api-response';
import { ProductModel, BasicProductInfo, DetailedProductInfo, PricingInfo, CategoryModelReq } from '../../models/product.model';
import { List } from 'lodash';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private _HttpClient: HttpClient) { }
  getProduct(page: number, pageSize: number): Observable<ApiResponse<ProductModel[]>> {
    return this._HttpClient
      .get<ApiResponse<ProductModel[]>>(`${environment.api}/products?page=${page}&pageSize=${pageSize}`);
  }

  getCategories(): Observable<List<CategoryModelReq>> {
    return this._HttpClient
      .get<List<CategoryModelReq>>(`${environment.api}/products/categories`);
  }

  getCategoryById(id: string): Observable<CategoryModelReq> {
    return this._HttpClient
      .get<CategoryModelReq>(`${environment.api}/products/categories/${id}`);
  }

  addCategory(category: CategoryModelReq): Observable<CategoryModelReq> {
    return this._HttpClient
      .post<CategoryModelReq>(`${environment.api}/products/categories`, category);
  }

  
  updateCategory(category: CategoryModelReq): Observable<CategoryModelReq> {
    return this._HttpClient
      .put<CategoryModelReq>(`${environment.api}/products/categories`, category);
  }
  getSingleProduct(id: number): Observable<ProductModel> {
    return this._HttpClient
      .get<ApiResponse<ProductModel>>(`${environment.api}/products/${id}`)
      .pipe(
        map((response: ApiResponse<ProductModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  getProductsByCategory(id: number): Observable<ProductModel[]> {
    return this._HttpClient
      .get<ApiResponse<ProductModel[]>>(`${environment.api}/products/category/${id}?page=0&pageSize=10`)
      .pipe(
        map((response: ApiResponse<ProductModel[]>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  // Create new product
  createProduct(product: ProductModel): Observable<ProductModel> {
    return this._HttpClient
      .post<ApiResponse<ProductModel>>(`${environment.api}/admin/products`, product)
      .pipe(
        map((response: ApiResponse<ProductModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  // Update basic product info
  updateBasicInfo(productId: number, basicInfo: BasicProductInfo): Observable<ProductModel> {
    return this._HttpClient
      .patch<ApiResponse<ProductModel>>(`${environment.api}/admin/products/${productId}/basic`, basicInfo)
      .pipe(
        map((response: ApiResponse<ProductModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  // Update detailed product info
  updateDetailedInfo(productId: number, detailedInfo: DetailedProductInfo): Observable<ProductModel> {
    return this._HttpClient
      .patch<ApiResponse<ProductModel>>(`${environment.api}/admin/products/${productId}/details`, detailedInfo)
      .pipe(
        map((response: ApiResponse<ProductModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  // Update inventory
  updateInventory(productId: number, stock: number): Observable<ProductModel> {
    return this._HttpClient
      .patch<ApiResponse<ProductModel>>(`${environment.api}/admin/products/${productId}/inventory`, { stock })
      .pipe(
        map((response: ApiResponse<ProductModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  // Update pricing
  updatePricing(productId: number, pricing: PricingInfo): Observable<ProductModel> {
    return this._HttpClient
      .patch<ApiResponse<ProductModel>>(`${environment.api}/admin/products/${productId}/pricing`, pricing)
      .pipe(
        map((response: ApiResponse<ProductModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }
}
