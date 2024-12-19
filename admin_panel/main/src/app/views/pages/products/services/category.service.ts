// services/category.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoryModelReq, CategoryModelRes } from '../../models/product.model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${environment.api}/category`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<CategoryModelRes[]> {
    return this.http.get<CategoryModelRes[]>(this.apiUrl);
  }

  createCategory(category: CategoryModelReq): Observable<CategoryModelReq> {
    return this.http.post<CategoryModelReq>(this.apiUrl, category);
  }

  updateCategory(id: string, category: CategoryModelReq): Observable<CategoryModelReq> {
    return this.http.put<CategoryModelReq>(`${this.apiUrl}/${id}`, category);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}