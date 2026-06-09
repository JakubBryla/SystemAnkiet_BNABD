import { Component, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Auth } from '../../services/auth';

export function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, RouterModule, ReactiveFormsModule, MatIconModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private auth = inject(Auth);

  registerForm = new FormGroup({
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', [Validators.required])
  }, { validators: passwordsMatchValidator }); 

  serverError = signal<string | null>(null);

  onSubmit() {
    if (this.registerForm.valid) {
      this.serverError.set(null); // Czyszczenie błędów przed wysłaniem
      const userData = {
        firstName: this.registerForm.value.firstName,
        lastName: this.registerForm.value.lastName,
        email: this.registerForm.value.email,
        password: this.registerForm.value.password
      };

      // --- SYMULACJA WALIDACJI DOMENY ---
      // Docelowo ten błąd wyrzuci backend, my go tylko przechwycimy.
      // Teraz symulujemy to na frontendzie, zakładając, że dozwolona domena to "nazwafirmy.pl"
      const email = userData.email || '';
      const domain = email.split('@')[1];

      if (domain !== 'nazwafirmy.pl') {
        this.serverError.set('Rejestracja dozwolona tylko dla autoryzowanych domen firmowych (np. @nazwafirmy.pl).');
        return;
      }
      // ----------------------------------

      this.auth.register(userData);
    }
  }
}
