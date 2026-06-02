import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiUrl = 'http://localhost:8080/api/auth';

  private http = inject(HttpClient);
  private router = inject(Router);

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  login(credentials: any) {
    this.http.post<{ token: string; email: string; role: string }>(
      `${this.apiUrl}/login`, credentials
    ).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Błąd logowania:', err);
        const msg = err.error?.error || 'Nieprawidłowy email lub hasło';
        alert(msg);
      }
    });
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

  register(userData: any) {
    this.http.post(`${this.apiUrl}/register`, userData).subscribe({
      next: () => {
        alert('Rejestracja zakończona sukcesem! Możesz się zalogować.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Błąd rejestracji:', err);
        const msg = err.error?.error || 'Błąd rejestracji. Spróbuj ponownie.';
        alert(msg);
      }
    });
  }
}
