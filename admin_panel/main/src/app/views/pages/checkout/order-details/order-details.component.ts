import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { OrdersService } from '../orders.service';
import { OrderModel, UpdateOrderStatusRequest } from '../../models/order.model';
import { HotToastService } from '@ngneat/hot-toast';
import { FormsModule } from '@angular/forms';
import { OrderStatus } from '../../models/order.types';

@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule]
})
export class OrderDetailsComponent implements OnInit {
  order?: OrderModel;
  isLoading = false;
  isEditing = false;
  OrderStatus = OrderStatus; // Make enum available in template

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private ordersService: OrdersService,
    private toast: HotToastService
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.loadOrder(params['id']);
      }
    });
  }
  
  loadOrder(orderNumber: string) {
    this.isLoading = true;
    this.ordersService.getSingleOrder(orderNumber).subscribe({
      next: (order) => {
        this.order = order;
        this.isLoading = false;
      },
      error: (error) => {
        this.toast.error('Failed to load order details');
        this.isLoading = false;
      }
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
  }

  updateOrderStatus(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    if (!this.order) return;
    this.ordersService.updateOrderStatus(this.order.orderNumber, { status: value as OrderStatus, updatedBy: 'admin' } as UpdateOrderStatusRequest).subscribe({
      next: () => {
        this.toast.success('Order status updated successfully');
        this.loadOrder(this.order!.orderNumber); // Reload order
      },
      error: (error) => {
        this.toast.error('Failed to update order status');
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

  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleString();
  }

   formatMoney(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'NPR'
    }).format(amount / 100);
  }
}
