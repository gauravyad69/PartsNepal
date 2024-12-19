import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app-routing.module';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { HotToastModule } from '@ngneat/hot-toast';
import { jwtInterceptor } from './views/pages/auth/services/jwt.interceptor';
import { AuthService } from './views/pages/auth/services/auth.service';
import { LocalstorageService } from './views/pages/auth/services/localstorage.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    importProvidersFrom(HotToastModule.forRoot()),
    AuthService,
    LocalstorageService
  ]
}; 