import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { HttpClient } from '@angular/common/http';
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
  createdAt: string;
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
export class Dashboard implements OnInit {
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = 'http://localhost:8080/api/surveys';

  surveys: SurveySummary[] = [];
  assignedSurveys: SurveySummary[] = [];

  searchQuery = '';
  sortBy: 'title' | 'status' | 'accessType' = 'title';
  sortDirection: 'asc' | 'desc' = 'asc';
  filterStatus: 'all' | 'draft' | 'active' | 'closed' = 'all';
  filterAccessType: 'all' | 'INTERNAL' | 'EXTERNAL' = 'all';
  pageSize = 6;
  pageIndex = 0;

  ngOnInit() {
    this.loadSurveys();
  }

  private loadSurveys() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.surveys = data.map(s => this.mapToSummary(s));
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd ładowania ankiet:', err)
    });

    this.http.get<any[]>(`${this.apiUrl}/assigned`).subscribe({
      next: (data) => {
        this.assignedSurveys = data.map(s => this.mapToSummary(s));
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd ładowania przypisanych ankiet:', err)
    });
  }

  // Backend zwraca type jako lowercase ('internal'/'external'), mapujemy na UPPERCASE dla UI
  private mapToSummary(s: any): SurveySummary {
    return {
      id: s.id,
      title: s.title,
      description: s.description ?? '',
      status: s.status,
      accessType: (s.type ?? 'external').toUpperCase() as 'INTERNAL' | 'EXTERNAL',
      createdAt: s.createdAt
    };
  }

  changeSurveyStatus(survey: SurveySummary, newStatus: 'draft' | 'active' | 'closed') {
    this.http.patch<any>(`${this.apiUrl}/${survey.id}/status`, { status: newStatus }).subscribe({
      next: (updated) => {
        survey.status = updated.status;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd zmiany statusu:', err)
    });
  }

  openCreateSurveyDialog() {
    const dialogRef = this.dialog.open(CreateSurveyDialog, { width: '400px' });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.http.post<any>(this.apiUrl, { title: result, description: '' }).subscribe({
          next: (created) => {
            this.surveys = [...this.surveys, this.mapToSummary(created)];
            this.cdr.detectChanges();
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
          },
          error: (err) => console.error('Błąd usuwania ankiety:', err)
        });
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
    }).catch(err => console.error('Błąd podczas kopiowania linku:', err));
  }

  get processedSurveys(): SurveySummary[] {
    const filtered = this.surveys.filter(s => {
      const matchSearch = s.title.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchStatus = this.filterStatus === 'all' || s.status === this.filterStatus;
      const matchAccess = this.filterAccessType === 'all' || s.accessType === this.filterAccessType;
      return matchSearch && matchStatus && matchAccess;
    });

    return filtered.sort((a, b) => {
      let comp = 0;
      if (this.sortBy === 'title') comp = a.title.localeCompare(b.title);
      else if (this.sortBy === 'status') comp = a.status.localeCompare(b.status);
      else if (this.sortBy === 'accessType') comp = a.accessType.localeCompare(b.accessType);
      return this.sortDirection === 'asc' ? comp : -comp;
    });
  }

  get paginatedSurveys(): SurveySummary[] {
    const start = this.pageIndex * this.pageSize;
    return this.processedSurveys.slice(start, start + this.pageSize);
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}
