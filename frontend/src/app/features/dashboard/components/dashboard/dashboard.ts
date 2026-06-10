import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
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

// Struktura odpowiedzi Spring Data Page<T> zwracanej przez backend
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

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
  private router = inject(Router);

  private apiUrl = 'http://localhost:8080/api/surveys';

  // Wszystkie ankiety załadowane z backendu (po filtrach/sortowaniu po stronie serwera)
  allSurveys: SurveySummary[] = [];

  // Getter zwraca tylko wycinek tablicy odpowiadający bieżącej stronie
  get paginatedSurveys(): SurveySummary[] {
    const start = this.pageIndex * this.pageSize;
    return this.allSurveys.slice(start, start + this.pageSize);
  }

  assignedSurveys: SurveySummary[] = [];

  // Filtry — każda zmiana trigguje nowe żądanie HTTP do backendu
  searchQuery = '';
  sortBy: 'title' | 'status' | 'accessType' = 'title';
  sortDirection: 'asc' | 'desc' = 'asc';
  filterStatus: 'all' | 'draft' | 'active' | 'closed' = 'all';
  filterAccessType: 'all' | 'INTERNAL' | 'EXTERNAL' = 'all';
  pageSize = 6;
  pageIndex = 0;

  get isAnkieterOrAdmin(): boolean {
    const role = localStorage.getItem('role');
    return role === 'SURVEYOR' || role === 'ADMIN';
  }

  ngOnInit() {
    // Zwykły użytkownik nie ma tu czego szukać — przekieruj na /my-surveys
    if (localStorage.getItem('role') === 'USER') {
      this.router.navigate(['/my-surveys']);
      return;
    }
    this.reloadSurveys();
    this.loadAssignedSurveys();
  }

  // Pobiera WSZYSTKIE ankiety pasujące do filtrów (size=1000 — paginacja odbywa się po stronie frontendu).
  // Backend nadal obsługuje filtrowanie i sortowanie; frontend tnie wyniki na strony przez paginatedSurveys.
  // Dzięki temu nawigacja między stronami nie wymaga kolejnych zapytań HTTP.
  private reloadSurveys() {
    if (!this.isAnkieterOrAdmin) return;

    this.http.get<PageResponse<any>>(this.apiUrl, {
      params: {
        page: 0,
        size: 1000,
        search: this.searchQuery,
        status: this.filterStatus,
        type: this.filterAccessType,
        sort: this.sortBy,
        dir: this.sortDirection
      }
    }).subscribe({
      next: (pageResult) => {
        this.allSurveys = pageResult.content.map(s => this.mapToSummary(s));
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd ładowania ankiet:', err)
    });
  }

  // Ankiety wewnętrzne do wypełnienia — osobna lista, bez paginacji (zwykle mała liczba)
  private loadAssignedSurveys() {
    this.http.get<any[]>(`${this.apiUrl}/assigned`).subscribe({
      next: (data) => {
        this.assignedSurveys = data.map(s => this.mapToSummary(s));
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd ładowania przypisanych ankiet:', err)
    });
  }

  // Wywoływane przez filtry (search, status, typ, sort) — resetuje stronę i pobiera wyniki
  onFilterChange() {
    this.pageIndex = 0;
    this.reloadSurveys();
  }

  // Przełącza kierunek sortowania i odświeża listę
  toggleSortDirection() {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    this.pageIndex = 0;
    this.reloadSurveys();
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
          next: () => {
            // Po dodaniu odśwież całą listę (prawidłowa paginacja i totalElements)
            this.reloadSurveys();
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
            // Po usunięciu odśwież — może zmienić się liczba stron
            this.reloadSurveys();
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

  // Zmiana strony/rozmiaru strony — tylko aktualizuje stan lokalny,
  // nie wysyła nowego zapytania HTTP (paginacja odbywa się w paginatedSurveys getter)
  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cdr.detectChanges();
  }
}
