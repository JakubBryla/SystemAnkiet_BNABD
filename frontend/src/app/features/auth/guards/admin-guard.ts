import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Auth } from '../services/auth';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.userRoleSignal() === 'ADMIN') {
    return true; 
  } else {
    console.warn('Odmowa dostępu: Brak uprawnień administratora.');
    router.navigate(['/dashboard']); 
    return false; 
  }
};