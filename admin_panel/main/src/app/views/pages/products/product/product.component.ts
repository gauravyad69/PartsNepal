import { Component, CUSTOM_ELEMENTS_SCHEMA, Input, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartItem } from '../../models/cart';
import { WishItem } from '../../models/wishlist';
import { HotToastService } from '@ngneat/hot-toast';
import { ProductModel } from '../../models/product.model';
import { NgOptimizedImage } from '@angular/common';
import { PriceFormatPipe } from '../pipe/price-format.pipe';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss'],
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe,
    NgOptimizedImage,
    PriceFormatPipe
  ]
})
export class ProductComponent {
  @Input() product!: ProductModel;
  isProductInWishList: boolean = false;
  WishItems!: WishItem[];

  constructor(
    private _toast: HotToastService
  ) { }


  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/images/ImageNotFound.png';
  }


}