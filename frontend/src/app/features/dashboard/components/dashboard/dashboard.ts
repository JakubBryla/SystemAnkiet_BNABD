import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatChipsModule } from '@angular/material/chips';
import { RouterModule } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CreateSurveyDialog } from '../create-survey-dialog/create-survey-dialog';
import { DeleteSurveyDialog } from '../delete-survey-dialog/delete-survey-dialog';

export interface SurveySummary {
  id: number;
  title: string;
  description: string;
  status: 'draft' | 'active' | 'closed';
  accessType: 'INTERNAL' | 'EXTERNAL'; 
  organizationName?: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatChipsModule,
    RouterModule,
    NgClass,
    MatSnackBarModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private cdr = inject(ChangeDetectorRef);
  // mock baza danych ankiet na potrzeby frontendu
  surveys: SurveySummary[] = [
    { 
      id: 1, 
      title: 'Satysfakcja z pakietu medycznego 2026', 
      description: 'Badanie wewnętrzne dla pracowników pionu logistyki.', 
      status: 'draft',
      accessType: 'INTERNAL',
      organizationName: 'ORLEN S.A.'
    },
    { 
      id: 2, 
      title: 'Opinia o paliwach VERVA i Stop Cafe', 
      description: 'Ogólnopolskie badanie opinii konsumentów i kierowców.', 
      status: 'active',
      accessType: 'EXTERNAL',
      organizationName: 'ORLEN S.A.'
    },
    { 
      id: 3, 
      title: 'Badanie komunikacji wewnętrznej', 
      description: 'Ankieta oceniająca nowy intranet firmowy.', 
      status: 'closed',
      accessType: 'INTERNAL',
      organizationName: 'ORLEN S.A.'
    }
  ];

  changeSurveyStatus(survey: SurveySummary, newStatus: 'draft' | 'active' | 'closed') {
    survey.status = newStatus;
    
    console.log(`AKTUALIZACJA STATUSU`);
    console.log(`Ankieta ID: ${survey.id} zmieniła status na: ${newStatus}`);
    
    // W przyszłościw wywołać API do aktualizacji statusu ankiety w backendzie
  }

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
            description: '',
            status: 'draft',
            accessType: 'EXTERNAL', 
            organizationName: 'ORLEN S.A.' 
          }
        ];
        this.cdr.detectChanges();
        
        console.log('Nowa ankieta została pomyślnie dodana!');
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
        
        // Zastąpić poniższy kod rzeczywistym wywołaniem API do usunięcia ankiety z backendu
        // Zostawiamy w tablicy tylko te ankiety, których ID nie równa się usuwanemu ID
        this.surveys = this.surveys.filter(s => s.id !== surveyId);
        
        // odświeżenie kafelków po usunięciu ankiety
        this.cdr.detectChanges();
        
        console.log(`Ankieta "${surveyTitle}" została usunięta!`);
      }
    });
  }

  shareSurvey(surveyId: number) {
    const url = `${window.location.origin}/s/${surveyId}`;
    
    navigator.clipboard.writeText(url).then(() => {
      this.snackBar.open('🔗 Link skopiowany do schowka!', 'OK', {
        duration: 3000,
        horizontalPosition: 'center',
        verticalPosition: 'bottom'
      });
    }).catch(err => {
      console.error('Błąd podczas kopiowania linku: ', err);
    });
  }
}
