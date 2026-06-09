import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private auth = inject(Auth);

  loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required])
  });

  serverError = signal<string | null>(null);
  isLoading = signal<boolean>(false);

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.serverError.set(null);
    this.isLoading.set(true);

    this.auth.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.isLoading.set(false);
        // Nawigacja jest obsługiwana w auth.ts (tap operator)
      },
      error: (err) => {
        this.isLoading.set(false);
        const msg = err.error?.error || 'Nieprawidłowy adres e-mail lub hasło.';
        this.serverError.set(msg);
      }
    });
  }
}
