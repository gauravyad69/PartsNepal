import { Component, CUSTOM_ELEMENTS_SCHEMA, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HotToastService } from '@ngneat/hot-toast';
import { WishlistService } from '../../pages/services/wishlist.service';
import { CartService } from '../../pages/services/cart.service';
import { WishItem } from '../../pages/models/wishlist';
import { CartItem } from '../../pages/models/cart';
import { ConfirmModelComponent } from '../confirm-model/confirm-model.component';
import { ProductModel } from '../../pages/models/product.model';

@Component({
  selector: 'app-wishlist',
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.css'],
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    ConfirmModelComponent
  ]
})
export class WishlistComponent implements OnInit {
  wishCount = 0;
  opanWishlist: boolean = false;
  isVisable: boolean = false;
  wishList!: WishItem[];
  deleteProductId!: number;
  opanWishList: boolean = false;
  WishItems!: WishItem[];

  constructor(
    private _wishlistService: WishlistService,
    private _cartService: CartService,
    private _toast: HotToastService,
    private router: Router
  ) { }

  openWishlist() {
    this.getWishList();
    this.opanWishlist = true;
    document.body.style.overflowY = "hidden";
  }

  closeSidebar() {
    this.opanWishlist = false;
    document.body.style.overflowY = "auto";
  }

  getWishList() {
    this._wishlistService.wishList$.subscribe((wishlist) => {
      this.wishList = wishlist.items!;
    });
  }

  deleteWishItem() {
    this._wishlistService.deleteWishItem(this.deleteProductId);
    this.closeCofirmModal();
    this._toast.error('Product removed from wishlist',
      {
        position: 'top-left'
      });
  }

  addToCart(item: WishItem) {
    const cartItem: CartItem = {
      product: item.product,
      quantity: 1
    };
    this._cartService.setCartItem(cartItem);
    this._toast.success('Product added to cart successfully',
      {
        position: 'top-left'
      });
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
  productInCartList(product: ProductModel){
    const cartItemExist = this.WishItems.find((item) => item.product?.basic?.productId === product.basic?.productId);
    return cartItemExist;
  }

  ngOnInit(): void {
    this._wishlistService.wishList$.subscribe((wishlist) => {
      this.wishCount = wishlist?.items?.length ?? 0;
    });
    this.getWishList();
  }
}
