import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { LocalstorageService } from './localstorage.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const localStorageService = inject(LocalstorageService);
  const token = localStorageService.getToken();

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
