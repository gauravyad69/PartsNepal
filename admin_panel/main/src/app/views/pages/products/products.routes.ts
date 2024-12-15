import { Routes } from '@angular/router';
import { AllProductsComponent } from './all-products/all-products.component';
import { ProductDetailsComponent } from './product-details/product-details.component';

export const PRODUCT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: AllProductsComponent
      },
      {
        path: ':id',
        component: ProductDetailsComponent
      }
    ]
  }
]; 