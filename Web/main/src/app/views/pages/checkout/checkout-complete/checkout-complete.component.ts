import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { CheckoutService } from '../checkout.service';
import { PaymentMethod } from '../../models/order.types';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-checkout-complete',
  templateUrl: './checkout-complete.component.html',
  styleUrls: ['./checkout-complete.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class CheckoutCompleteComponent implements OnInit, OnDestroy {
  orderNumber?: string;
  paymentMethod?: PaymentMethod;
  today: number = Date.now();
  private subscription: Subscription | null = null;

  constructor(
    private router: Router,
    private _cartService: CartService,
    private _checkoutService: CheckoutService
  ) { }

  ngOnInit(): void {
    this.subscription = this._checkoutService.checkoutDetails$.subscribe(
      details => {
        if (!details?.orderNumber) {
          // If no order details, redirect back to checkout
          this.router.navigate(['/checkout']);
          return;
        }
        this.orderNumber = details.orderNumber;
        this.paymentMethod = details.paymentMethod;
      }
    );
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this._checkoutService.clearCheckoutDetails();
  }

  getPaymentMethodDisplay(method: PaymentMethod): string {
    switch(method) {
      case PaymentMethod.CASH_ON_DELIVERY:
        return 'Cash on Delivery';
      case PaymentMethod.KHALTI:
        return 'Khalti';
      case PaymentMethod.ESEWA:
        return 'eSewa';
      default:
        return 'Unknown';
    }
  }

  navigateToStore() {
    // this._cartService.clearCart();
    this.router.navigate(['/']);
  }
}
