import { ProductModel } from "./product.model";

export class Cart {
  items?: CartItem[];
}

export class CartItem {
  product?: ProductModel;
  quantity?: number;
}

export class CartItemDetailed {
  product?: ProductModel;
  quantity?: number;
}
