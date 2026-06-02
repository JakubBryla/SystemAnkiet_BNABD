import { Component, inject, ChangeDetectorRef, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { CreateSurveyDialog } from '../create-survey-dialog/create-survey-dialog';
import { DeleteSurveyDialog } from '../delete-survey-dialog/delete-survey-dialog';

@Component({
  selector: 'app-dashboard',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private dialog = inject(MatDialog);
  private cdr = inject(ChangeDetectorRef);
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8080/api/surveys';

  surveys: { id: number; title: string; status: string; responses: number }[] = [];

  ngOnInit() {
    this.loadSurveys();
  }

  loadSurveys() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.surveys = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd ładowania ankiet:', err)
    });
  }

  openCreateSurveyDialog() {
    const dialogRef = this.dialog.open(CreateSurveyDialog, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.http.post<any>(this.apiUrl, { title: result }).subscribe({
          next: (created) => {
            this.surveys = [...this.surveys, created];
            this.cdr.detectChanges();
            console.log('Nowa ankieta została pomyślnie dodana!');
          },
          error: (err) => console.error('Błąd tworzenia ankiety:', err)
        });
      }
    });
  }

  deleteSurvey(surveyId: number, surveyTitle: string) {
    const dialogRef = this.dialog.open(DeleteSurveyDialog, {
      width: '400px',
      data: { title: surveyTitle }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.http.delete(`${this.apiUrl}/${surveyId}`).subscribe({
          next: () => {
            this.surveys = this.surveys.filter(s => s.id !== surveyId);
            this.cdr.detectChanges();
            console.log(`Ankieta "${surveyTitle}" została usunięta!`);
          },
          error: (err) => console.error('Błąd usuwania ankiety:', err)
        });
      }
    });
  }
}
