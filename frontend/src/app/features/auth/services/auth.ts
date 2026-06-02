import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  //w przyszłości adres do backendu
  private apiUrl = 'http://localhost:8080/api/auth';

  private http = inject(HttpClient);
  private router = inject(Router);

  // mock zmienna udająca stan zalogowania - w przyszłości będzie to token JWT 
  public isLoggedInSignal = signal<boolean>(false);

  isLoggedIn(): boolean {
    // w przyszłości będziemy tu sprawdzany prawdziwy token JWT z przeglądarki
    return this.isLoggedInSignal();
  }

  login(credentials: any) {
    console.log('AuthService (Logowanie): Wysyłam dane do', this.apiUrl + '/login');
    // odkomentować to w przyszłości:
    // return this.http.post(`${this.apiUrl}/login`, credentials);

    // symulacja udanego logowania - w przyszłości usuniemy ten kod i będziemy polegać na odpowiedzi z backendu
    this.isLoggedInSignal.set(true); 
    this.router.navigate(['/dashboard']);
  }

  logout() {
    console.log('Wylogowywanie...');
    // w przyszłości usunąć token JWT z localStorage
    // localStorage.removeItem('token');
    this.isLoggedInSignal.set(false);
    this.router.navigate(['/login']);
  }

  register(userData: any) {
    console.log('AuthService (Rejestracja): Wysyłam dane do', this.apiUrl + '/register');
    // odkomentować to w przyszłości:
    // return this.http.post(`${this.apiUrl}/register`, userData);
  }
}
