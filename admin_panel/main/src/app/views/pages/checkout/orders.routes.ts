import { Routes } from '@angular/router';
import { OrdersComponent } from './orders.component';
import { AllOrdersComponent } from './all-orders/all-orders.component';
import { OrderDetailsComponent } from './order-details/order-details.component';

export const ORDER_ROUTES: Routes = [
  {
    path: '',
    component: OrdersComponent,
    children: [
      {
        path: '',
        component: AllOrdersComponent
      },
      {
        path: ':id',
        component: OrderDetailsComponent
      }
    ]
  }
]; 