import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrdersComponent } from './orders.component';
import { AllOrdersComponent } from './all-orders/all-orders.component';
import { OrderDetailsComponent } from './order-details/order-details.component';
import { ORDER_ROUTES } from './orders.routes';

@NgModule({
  declarations: [
    OrdersComponent,
    AllOrdersComponent,
    OrderDetailsComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(ORDER_ROUTES)
  ]
})
export class OrdersModule { }
