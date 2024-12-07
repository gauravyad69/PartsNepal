import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';
// import { ApiResponse } from '../../models/api-response';
import { OrderModel, CreateOrderRequest } from '../models/order.model';
import { ApiResponse } from '../models/api-response';
@Injectable({
  providedIn: 'root'
})
export class CheckoutService {

  constructor(private _HttpClient: HttpClient) { }
  getAllOrders(page: number, pageSize: number): Observable<OrderModel[]> {
    return this._HttpClient
      .get<ApiResponse<OrderModel[]>>(`${environment.api}/orders?page=${page}&pageSize=${pageSize}`)
      .pipe(
        map((response: ApiResponse<OrderModel[]>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  getSingleOrder(id: number): Observable<OrderModel> {
    return this._HttpClient
      .get<ApiResponse<OrderModel>>(`${environment.api}/orders/${id}`)
      .pipe(
            map((response: ApiResponse<OrderModel>) => {
          if (!response.data) {
            throw new Error('Invalid API response format');
          }
          return response.data;
        })
      );
  }

  createOrder(order: CreateOrderRequest): Observable<OrderModel> {
    return this._HttpClient
      .post<OrderModel>(`${environment.api}/orders`, order)
      .pipe(
        map((response: OrderModel) => {
          if (!response) {
            throw new Error('Invalid API response format');
          }
          return response;
        })
      );
  }
}
