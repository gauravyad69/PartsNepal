import { Component, CUSTOM_ELEMENTS_SCHEMA, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HotToastService } from '@ngneat/hot-toast';
import { CartItem } from '../../pages/models/cart';
import { CartService } from '../../pages/services/cart.service';
import { NgxSkeletonLoaderModule } from 'ngx-skeleton-loader';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css'],
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    NgxSkeletonLoaderModule
  ]
})
export class CartComponent implements OnInit {
  cartCount = 0;
  totalPrice!: number;
  opanCartlist: boolean = false;
  isVisable: boolean = false;
  cartList!: CartItem[];
  deleteProductId!: number;

  constructor(
    private _cartService: CartService,
    private _toast: HotToastService,
    private router: Router
  ) { }

  openCartlist() {
    this.getCartList();
    this.opanCartlist = true;
    document.body.style.overflowY = "hidden";
  }

  closeSidebar() {
    this.opanCartlist = false;
    document.body.style.overflowY = "auto";
  }

  getCartList() {
    this._cartService.cart$.subscribe((cart) => {
      this.cartList = cart.items!;
    });
  }

  deleteCartItem() {
    this._cartService.deleteCartItem(this.deleteProductId);
    this.closeCofirmModal();
    this._toast.error('Product removed from cart',
      {
        position: 'top-left'
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

  updateCartItemQuantity(value: number, cartItem: CartItem, operation: string) {
    if (operation == "+") {
      value++;
    } else {
      value--;
    }
    this._cartService.setCartItem(
      {
        product: cartItem.product,
        quantity: value,
      },
      true
    );
  }

  navigateToCheckout() {
    this.closeSidebar();
    this.router.navigate(['/checkout']);
  }

  navigateToProductDetails(productId: number) {
    this.closeSidebar();
    this.router.navigate(['/products', productId]);
  }

  openCofirmModal(productId: number) {
    this.isVisable = true;
    this.deleteProductId = productId;
  }

  closeCofirmModal() {
    this.isVisable = false;
  }

  ngOnInit(): void {
    this._cartService.cart$.subscribe((cart) => {
      this.cartCount = cart?.items?.length ?? 0;
    });
    this.getCartList();
    this.getTotalPrice();
  }
}
