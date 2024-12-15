import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';

@Component({
  selector: 'app-checkout-payment',
  templateUrl: './checkout-payment.component.html',
  styleUrls: ['./checkout-payment.component.css'],
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [CommonModule, RouterModule, NgxSkeletonLoaderModule]
})
export class CheckoutPaymentComponent implements OnInit {

  totalPrice!: number;
  today: number = Date.now();

  constructor(
    private router: Router,
    private _cartService: CartService,

  ) { }

  navigateToStore() {
    this.router.navigate(['/'])
  }

  getTotalPrice() {
    this._cartService.cart$.subscribe((cart) => {
      this.totalPrice = 0;
      if (cart) {
        cart.items?.map((item) => {
          this.totalPrice += item.product?.basic?.pricing?.regularPrice?.amount!! * item.quantity!;
        });
      }
    });
  }
  ngOnInit(): void {
    this.getTotalPrice();
  }

}
