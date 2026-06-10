import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

export interface AssignedSurvey {
  id: number;
  title: string;
  description: string;
  status: 'draft' | 'active' | 'closed';
}

@Component({
  selector: 'app-respondent-dashboard',
  imports: [
    RouterLink,
    NgClass,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    FormsModule
  ],
  templateUrl: './respondent-dashboard.html',
  styleUrl: './respondent-dashboard.scss',
})
export class RespondentDashboard implements OnInit {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = 'http://localhost:8080/api/surveys/assigned';

  surveys: AssignedSurvey[] = [];
  isLoading = true;
  errorMessage: string | null = null;

  searchQuery = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  pageSize = 6;
  pageIndex = 0;

  ngOnInit() {
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.surveys = data.map(s => ({
          id: s.id,
          title: s.title,
          description: s.description ?? '',
          status: s.status
        }));
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Nie można załadować ankiet. Spróbuj ponownie.';
        this.cdr.detectChanges();
      }
    });
  }

  get processedSurveys(): AssignedSurvey[] {
    const filtered = this.surveys.filter(s =>
      s.title.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
    return [...filtered].sort((a, b) => {
      const comp = a.title.localeCompare(b.title);
      return this.sortDirection === 'asc' ? comp : -comp;
    });
  }

  get paginatedSurveys(): AssignedSurvey[] {
    const start = this.pageIndex * this.pageSize;
    return this.processedSurveys.slice(start, start + this.pageSize);
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}
