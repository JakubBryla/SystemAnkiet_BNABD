import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { CreateSurveyDialog } from '../create-survey-dialog/create-survey-dialog';

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
export class Dashboard {
  private dialog = inject(MatDialog);
  private cdr = inject(ChangeDetectorRef);
  // mock baza danych ankiet na potrzeby frontendu
  surveys = [
    { id: 1, title: 'Ankieta satysfakcji klienta', status: 'Aktywna', responses: 15 },
    { id: 2, title: 'Badanie rynku IT 2026', status: 'Zakończona', responses: 142 },
    { id: 3, title: 'Ocena szkolenia', status: 'Szkic', responses: 0 }
  ];

  openCreateSurveyDialog() {
    const dialogRef = this.dialog.open(CreateSurveyDialog, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Obliczanie nowego ID (największe obecne + 1)
        const newId = this.surveys.length > 0 ? Math.max(...this.surveys.map(s => s.id)) + 1 : 1;
        
        // Dodanie nowej ankietę do mock bazy
        this.surveys = [
          ...this.surveys, 
          {
            id: newId,
            title: result,
            status: 'Szkic',
            responses: 0
          }
        ];
        this.cdr.detectChanges();
        
        console.log('Nowa ankieta została pomyślnie dodana!');
      }
    });
  }
}
