import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CartService } from '../../services/cart.service';
import { CheckoutService } from '../checkout.service';
import { OrderSource, ShippingMethod, PaymentMethod } from '../../models/order.types';
import { CreateOrderRequest, LineItem } from '../../models/order.model';
import { CartItem } from '../../models/cart';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';

@Component({
  selector: 'app-checkout-page',
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    NgxSkeletonLoaderModule
  ]
})
export class CheckoutPageComponent {

  checkoutFormGroup!: FormGroup;
  isSubmitted = false;
  cartList!: CartItem[];
  totalPrice!: number;
  isCartEmpty: boolean = false;
  ShippingMethod = ShippingMethod;
  PaymentMethod = PaymentMethod;

  constructor(
    private router: Router,
    private _cartService: CartService,
    private formBuilder: FormBuilder,
    private _checkoutService: CheckoutService
  ) { }

  getCartList() {
    this._cartService.cart$.subscribe((cart) => {
      this.cartList = cart.items!;
      if (this.cartList.length == 0) this.isCartEmpty = true;
      else this.isCartEmpty = false;
    });
  }

  getTotalPrice() {
    this._cartService.cart$.subscribe((cart) => {
      this.totalPrice = 0;
      if (cart) {
        cart.items?.map((item) => {
          this.totalPrice += item.product?.basic?.pricing?.regularPrice?.amount! * item.quantity!;
        });
      }
    });
  }


  initCheckoutForm() {
    this.checkoutFormGroup = this.formBuilder.group({
      recipientName: ['', Validators.required],
      recipientPhone: ['', Validators.required],
      street: ['', Validators.required],
      city: ['', Validators.required],
      district: ['', Validators.required],
      province: ['', Validators.required],
      ward: ['', Validators.required],
      landmark: [''],
      shippingMethod: [ShippingMethod.STANDARD, Validators.required],
      paymentMethod: [PaymentMethod.CASH_ON_DELIVERY, Validators.required],
      notes: ['']
    });
  }


  get checkoutForm() {
    return this.checkoutFormGroup.controls;
  }
  placeOrder() {
    this.isSubmitted = true;
    if (this.checkoutFormGroup.invalid) {
      return;
    }

    const items: LineItem[] = this.cartList.map((item) => ({
      productId: item.product?.basic?.productId!,
      name: item.product?.basic.productName || 'Untitled Product',
      quantity: item.quantity!,
      unitPrice: {
        amount: item.product?.basic?.pricing?.regularPrice?.amount!,
        currency: 'NPR'
      },
      imageUrl: item.product?.basic.inventory.mainImage || '',
      lastModified: Date.now()
    }));

    const order: CreateOrderRequest = {
      items,
      paymentMethod: this.checkoutForm.paymentMethod.value,
      shippingDetails: {
        address: {
          street: this.checkoutForm.street.value,
          city: this.checkoutForm.city.value,
          district: this.checkoutForm.district.value,
          province: this.checkoutForm.province.value,
          ward: this.checkoutForm.ward.value,
          landmark: this.checkoutForm.landmark.value || undefined,
          recipient: {
            name: this.checkoutForm.recipientName.value,
            phone: this.checkoutForm.recipientPhone.value
          }
        },
        method: this.checkoutForm.shippingMethod.value,
        cost: {
          amount: this.totalPrice,
          currency: 'NPR'
        }
      },
      notes: this.checkoutForm.notes.value || undefined,
      source: OrderSource.WEB
    };

    // Store the payment method before making the API call
    const paymentMethod = this.checkoutForm.paymentMethod.value;

    this._checkoutService.createOrder(order).subscribe({
      next: (response) => {
        if (response.orderNumber != null) {
          // Set both payment method and order number
          this._checkoutService.setCheckoutDetails({
            orderNumber: response.orderNumber,
            paymentMethod: paymentMethod
          });
          this.router.navigate(['/checkout/success']);
        } else {
          console.error("Order creation failed");
        }
      },
      error: (error) => {
        console.error('Order creation failed:', error);
      }
    });
  }

  ngOnInit(): void {
    this.getCartList();
    this.getTotalPrice();
    this.initCheckoutForm();
  }


}
