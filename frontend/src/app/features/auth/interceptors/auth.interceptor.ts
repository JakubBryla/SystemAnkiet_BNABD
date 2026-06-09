import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TRUSTED_API_HOSTS } from '../../../core/constants/api.constants';

/**
 * Checks if the request URL is from a trusted API host
 */
function isTrustedApiUrl(url: string): boolean {
  try {
    if (url.startsWith('/api')) {
      return true;
    }
    const requestUrl = new URL(url);
    return TRUSTED_API_HOSTS.some(trustedHost => requestUrl.hostname === trustedHost);
  } catch {
    return false;
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  const cloned = (token && isTrustedApiUrl(req.url))
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && isTrustedApiUrl(req.url)) {
        // Token wygasł lub użytkownik wylogował się w innej karcie
        localStorage.removeItem('token');
        localStorage.removeItem('email');
        localStorage.removeItem('role');
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
