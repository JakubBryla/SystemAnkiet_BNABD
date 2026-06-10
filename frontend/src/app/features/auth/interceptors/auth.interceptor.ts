import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { TRUSTED_API_HOSTS } from '../../../core/constants/api.constants';
import { Auth } from '../services/auth';

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
  const authService = inject(Auth);
  const token = localStorage.getItem('token');

  const cloned = (token && isTrustedApiUrl(req.url))
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      // Łapiemy błąd 401 (Nieautoryzowany) oraz 403 (Zabroniony)
      if ((err.status === 401 || err.status === 403) && isTrustedApiUrl(req.url)) {
        console.warn('Odmowa dostępu z backendu (401/403). Wymuszam wylogowanie.');
        
        authService.logout();
      }
      return throwError(() => err);
    })
  );
};