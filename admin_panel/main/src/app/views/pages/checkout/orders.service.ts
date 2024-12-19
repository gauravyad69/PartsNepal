import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';
// import { ApiResponse } from '../../models/api-response';
import { OrderModel, CreateOrderRequest, UpdateOrderStatusRequest } from '../models/order.model';
import { ApiResponse } from '../models/api-response';
import { PaymentMethod } from '../models/order.types';
import { OrderStatus } from '../models/order.types';

interface OrderDetails {
  orderNumber?: string;
  paymentMethod: PaymentMethod;
}


@Injectable({
  providedIn: 'root'
})
export class OrdersService {


  private orderDetailsSubject = new BehaviorSubject<OrderDetails | null>(null);

  setOrderDetails(details: OrderDetails) {
    this.orderDetailsSubject.next(details);
  }

  clearOrderDetails() {
    this.orderDetailsSubject.next(null);
  }
  orderDetails$ = this.orderDetailsSubject.asObservable();



  constructor(private _HttpClient: HttpClient) { }
  getAllOrders(skip: number, limit: number): Observable<OrderModel[]> {
    return this._HttpClient
      .get<OrderModel[]>(`${environment.api}/admin/orders?skip=${skip}&limit=${limit}`)
      .pipe(
        map((response: OrderModel[]) => {
          if (!response) {
            throw new Error('Invalid API response format');
          }
          return response;
        })
      );
  }

  getSingleOrder(orderNumber: string): Observable<OrderModel> {
    return this._HttpClient
      .get<OrderModel>(`${environment.api}/admin/orders/${orderNumber}`)
      .pipe(
        map((response: OrderModel) => {
          if (!response) {
            throw new Error('Invalid API response format');
          }
          return response;
        })
      );
  }

  createOrder(order: CreateOrderRequest): Observable<OrderModel> {
    return this._HttpClient
      .post<OrderModel>(`${environment.api}/admin/orders`, order)
      .pipe(
        map((response: OrderModel) => {
          if (!response) {
            throw new Error('Invalid API response format');
          }
          return response;
        })
      );
  }

  updateOrderStatus(orderNumber: string, status: UpdateOrderStatusRequest): Observable<OrderModel> {
    return this._HttpClient
      .patch<OrderModel>(`${environment.api}/admin/orders/${orderNumber}/status`, status)
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
