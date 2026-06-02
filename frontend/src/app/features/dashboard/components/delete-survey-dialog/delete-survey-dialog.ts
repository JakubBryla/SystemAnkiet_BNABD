import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
// MAT_DIALOG_DATA do odbierania danych
import { MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-delete-survey-dialog',
  imports: [MatButtonModule, MatDialogModule],
  templateUrl: './delete-survey-dialog.html',
  styleUrl: './delete-survey-dialog.scss',
})
export class DeleteSurveyDialog {
  public data = inject(MAT_DIALOG_DATA);
}
