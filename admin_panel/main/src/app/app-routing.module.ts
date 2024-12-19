// app-routing.module.ts
import { Routes } from '@angular/router';
import { BaseComponent } from './views/layout/base/base.component';
import { AuthGuard } from './views/pages/auth/services/auth-guard.service';


export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./views/pages/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: '',
    component: BaseComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'products',
        loadChildren: () => import('./views/pages/products/products.routes')
          .then(m => m.PRODUCT_ROUTES)
      },
      {
        path: '',
        redirectTo: 'products',
        pathMatch: 'full'
      },
      {
        path: 'orders',
        loadChildren: () => import('./views/pages/checkout/orders.routes')
          .then(m => m.ORDER_ROUTES)
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'auth',
    pathMatch: 'full'
  }
];