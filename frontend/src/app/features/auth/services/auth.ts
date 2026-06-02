import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  //w przyszłości adres do backendu
  private apiUrl = 'http://localhost:8080/api/auth';

  private http = inject(HttpClient);

  login(credentials: any) {
    console.log('AuthService (Logowanie): Wysyłam dane do', this.apiUrl + '/login');
    // odkomentować to w przyszłości:
    // return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  register(userData: any) {
    console.log('AuthService (Rejestracja): Wysyłam dane do', this.apiUrl + '/register');
    // odkomentować to w przyszłości:
    // return this.http.post(`${this.apiUrl}/register`, userData);
  }
}
