import { Routes } from '@angular/router';
import { CheckoutComponent } from './checkout.component';
import { CheckoutPageComponent } from './checkout-page/checkout-page.component';
import { CheckoutCompleteComponent } from './checkout-complete/checkout-complete.component';
import { CheckoutPaymentComponent } from './checkout-payment/checkout-payment.component';

export const CHECKOUT_ROUTES: Routes = [
  {
    path: '',
    component: CheckoutComponent,
    children: [
      {
        path: '',
        component: CheckoutPageComponent
      },
      {
        path: 'success',
        component: CheckoutCompleteComponent
      },
      {
        path: 'payment',
        component: CheckoutPaymentComponent
      }
    ]
  }
]; 