import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

export function noWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
  const isWhitespace = (control.value || '').trim().length === 0;
  // Jeśli to same spacje, zwracamy błąd. Jeśli jest ok, zwracamy null
  return !isWhitespace ? null : { whitespace: true };
}

@Component({
  selector: 'app-create-survey-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './create-survey-dialog.html',
  styleUrl: './create-survey-dialog.scss',
})
export class CreateSurveyDialog {
  private dialogRef = inject(MatDialogRef<CreateSurveyDialog>);

  titleControl = new FormControl('', [Validators.required, noWhitespaceValidator]);

  onSave() {
    if (this.titleControl.valid) {
      const finalTitle = this.titleControl.value!.trim();
      this.dialogRef.close(finalTitle);
    }
  }
}
