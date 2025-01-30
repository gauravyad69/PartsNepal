// services/category.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CarrouselReq } from '../../models/product.model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CarrouselService {
  private apiUrl = `${environment.api}/admin/carrousel`;

  constructor(private http: HttpClient) {}

  getCarrousel(): Observable<CarrouselReq[]> {
    return this.http.get<CarrouselReq[]>(this.apiUrl);
  }

  createCarrousel(carrousel: CarrouselReq): Observable<CarrouselReq> {
      return this.http.post<CarrouselReq>(this.apiUrl, carrousel);
  }

  updateCarrousel(id: string, carrousel: CarrouselReq): Observable<CarrouselReq> {
    return this.http.put<CarrouselReq>(`${this.apiUrl}/${id}`, carrousel);
  }

  deleteCarrousel(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}