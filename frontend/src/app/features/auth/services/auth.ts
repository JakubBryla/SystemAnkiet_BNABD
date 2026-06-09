import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

interface AuthResponse {
  token: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiUrl = 'http://localhost:8080/api/auth';

  private http = inject(HttpClient);
  private router = inject(Router);

  public isLoggedInSignal = signal<boolean>(!!localStorage.getItem('token'));

  isLoggedIn(): boolean {
    return this.isLoggedInSignal();
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  login(credentials: { email: string | null | undefined; password: string | null | undefined }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('email', response.email);
        localStorage.setItem('role', response.role);
        this.isLoggedInSignal.set(true);

        // Jeśli użytkownik był przekierowany z ankiety — wróć do niej
        const returnUrl = sessionStorage.getItem('loginReturnUrl');
        if (returnUrl) {
          sessionStorage.removeItem('loginReturnUrl');
          this.router.navigateByUrl(returnUrl);
        } else {
          this.router.navigate(['/dashboard']);
        }
      })
    );
  }

  register(userData: {
    firstName?: string | null;
    lastName?: string | null;
    email?: string | null;
    password?: string | null;
  }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/register`, userData);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('email');
    localStorage.removeItem('role');
    this.isLoggedInSignal.set(false);
    this.router.navigate(['/login']);
  }
}
