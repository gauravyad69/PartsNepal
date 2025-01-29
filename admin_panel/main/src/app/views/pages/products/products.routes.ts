import { Routes } from '@angular/router';
import { AllProductsComponent } from './all-products/all-products.component';
import { ProductDetailsComponent } from './product-edit/product-details.component';
import { ProductAddComponent } from './product-add/product-add.component';
import { CategoryComponent } from './category/category.component';
import { CarrouselComponent } from './carrousel/carrousel.component';

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
        path: 'carrousel',
        component: CarrouselComponent
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