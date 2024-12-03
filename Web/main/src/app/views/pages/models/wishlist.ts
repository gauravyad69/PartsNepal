import { ProductModel } from "./product.model";

export class WishList {
    items?: WishItem[];
}

export class WishItem {
    product?: ProductModel;
}
