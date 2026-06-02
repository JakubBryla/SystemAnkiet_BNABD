import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';

import { Auth } from '../../services/auth';

export function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private auth = inject(Auth);

  registerForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', [Validators.required])
  }, { validators: passwordsMatchValidator }); 

  onSubmit() {
    if (this.registerForm.valid) {
      const userData = this.registerForm.value;
      
      this.auth.register(userData);
      
      console.log('Komponent Rejestracji: Przekazałem dane nowego użytkownika do serwisu!');
    } else {
      console.log('Formularz zawiera błędy!');
    }
  }
}
