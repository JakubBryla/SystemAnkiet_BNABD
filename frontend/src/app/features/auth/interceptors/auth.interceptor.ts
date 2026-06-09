import { HttpInterceptorFn } from '@angular/common/http';
import { TRUSTED_API_HOSTS } from '../../../core/constants/api.constants';

/**
 * Checks if the request URL is from a trusted API host
 */
function isTrustedApiUrl(url: string): boolean {
  try {
    // Handle relative URLs by checking if they start with /api
    if (url.startsWith('/api')) {
      return true;
    }

    // For absolute URLs, check the host
    const requestUrl = new URL(url);
    // hostname nie zawiera portu (np. "localhost"), host zawiera port ("localhost:8080")
    return TRUSTED_API_HOSTS.some(trustedHost => requestUrl.hostname === trustedHost);
  } catch {
    // If URL parsing fails, assume it's not trusted
    return false;
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');

  // Only add Authorization header for trusted API URLs
  if (token && isTrustedApiUrl(req.url)) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};
