import { Routes } from '@angular/router';
import { AllProductsComponent } from './all-products/all-products.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import { ProductAddComponent } from './product-add/product-add.component';
import { CategoryComponent } from './category/category.component';

export const PRODUCT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        component: AllProductsComponent
      },
      {
        path: 'categories',
        component: CategoryComponent
      },
      {
        path: 'add',
        component: ProductAddComponent
      },
      {
        path: 'details/:id',
        component: ProductDetailsComponent
      }
    ]
  }
]; 