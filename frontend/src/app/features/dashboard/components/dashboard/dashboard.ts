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
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

export interface SurveySummary {
  id: number;
  title: string;
  description: string;
  status: 'draft' | 'active' | 'closed';
  accessType: 'INTERNAL' | 'EXTERNAL'; 
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
    MatSnackBarModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule
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
      accessType: 'INTERNAL'
    },
    { 
      id: 2, 
      title: 'Opinia o paliwach VERVA i Stop Cafe', 
      description: 'Ogólnopolskie badanie opinii konsumentów i kierowców.', 
      status: 'active',
      accessType: 'EXTERNAL'
    },
    { 
      id: 3, 
      title: 'Badanie komunikacji wewnętrznej', 
      description: 'Ankieta oceniająca nowy intranet firmowy.', 
      status: 'closed',
      accessType: 'INTERNAL'
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
            accessType: 'EXTERNAL'
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

  searchQuery = '';
  sortBy: 'title' | 'status' | 'accessType' = 'title';
  sortDirection: 'asc' | 'desc' = 'asc';
  filterStatus: 'all' | 'draft' | 'active' | 'closed' = 'all';
  filterAccessType: 'all' | 'INTERNAL' | 'EXTERNAL' = 'all';
  pageSize = 6;
  pageIndex = 0;

  get processedSurveys() {
    // Filtrowanie
    const result = this.surveys.filter(s => {
      const matchSearch = s.title.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchStatus = this.filterStatus === 'all' || s.status === this.filterStatus;
      const matchAccess = this.filterAccessType === 'all' || s.accessType === this.filterAccessType;
      
      return matchSearch && matchStatus && matchAccess;
    });

    // Sortowanie
    [...result].sort((a, b) => {
      let comp = 0;
      if (this.sortBy === 'title') {
        comp = a.title.localeCompare(b.title);
      } else if (this.sortBy === 'status') {
        comp = a.status.localeCompare(b.status);
      } else if (this.sortBy === 'accessType') {
        comp = a.accessType.localeCompare(b.accessType);
      }
      return this.sortDirection === 'asc' ? comp : -comp;
    });

    return result;
  }

  get paginatedSurveys() {
    const start = this.pageIndex * this.pageSize;
    return this.processedSurveys.slice(start, start + this.pageSize);
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}
