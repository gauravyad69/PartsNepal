import { Component, OnInit } from '@angular/core';
import { OrdersService } from '../orders.service';
import { OrderModel } from '../../models/order.model';
import { HotToastService } from '@ngneat/hot-toast';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';
import { ProductComponent } from '../../products/product/product.component';
import { FilterPipe } from '../../products/pipe/filter.pipe';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { OrderStatus } from '../../models/order.types';

@Component({
  selector: 'app-all-orders',
  templateUrl: './all-orders.component.html',
  styleUrls: ['./all-orders.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NgxSkeletonLoaderModule,
    ProductComponent,
    FilterPipe,
    FormsModule,
    CurrencyPipe,
  ]
})
export class AllOrdersComponent implements OnInit {
  orders: OrderModel[] = [];
  currentPage = 1;
  limit = 10;
  isLoading = false;

  constructor(
    private ordersService: OrdersService,
    private toast: HotToastService
  ) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.isLoading = true;
    const skip = (this.currentPage - 1) * this.limit;
    this.ordersService.getAllOrders(skip, this.limit).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.isLoading = false;
      },
      error: (error) => {
        this.toast.error(`Failed to load orders ${error.message}`);
        this.isLoading = false;
      }
    });
  }

  getStatusClass(status: OrderStatus): string {
    switch(status) {
      case OrderStatus.PENDING_PAYMENT:
        return 'badge-warning';
      case OrderStatus.PAYMENT_CONFIRMED:
      case OrderStatus.DELIVERED:
        return 'badge-success';
      case OrderStatus.CANCELLED:
        return 'badge-danger';
      default:
        return 'badge-info';
    }
  }
  onPageChange(page: number) {
    this.currentPage = page;
    this.loadOrders();
  }
}
