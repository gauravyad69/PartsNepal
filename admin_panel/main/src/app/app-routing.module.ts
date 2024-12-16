import { Routes } from '@angular/router';
import { BaseComponent } from './views/layout/base/base.component';
import { ErrorPageComponent } from './views/pages/error-page/error-page.component';
import { AuthGuard } from './views/pages/auth/services/auth-guard.service';
import { ProductAddComponent } from './views/pages/products/product-add/product-add.component';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./views/pages/auth/auth.routes')
      .then((m) => m.AUTH_ROUTES)
  },
  {
    path: '',
    component: BaseComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'products',
        loadChildren: () => import('./views/pages/products/products.routes')
          .then((m) => m.PRODUCT_ROUTES)
      },
      {
        path: 'add',
        component: ProductAddComponent
      },
      {
        path: 'checkout',
        loadChildren: () => import('./views/pages/checkout/checkout.routes')
          .then((m) => m.CHECKOUT_ROUTES)
      },
      {
        path: 'user',
        loadChildren: () => import('./views/pages/user/user.routes')
          .then((m) => m.USER_ROUTES)
      },
      { path: '', redirectTo: 'products', pathMatch: 'full' }
    ]
  },
  {
    path: 'error',
    component: ErrorPageComponent,
    data: {
      type: 404,
      title: 'Page Not Found',
      desc: "Oopps!! The page you were looking for doesn't exist."
    }
  },
  { path: '**', redirectTo: 'auth', pathMatch: 'full' }
];
