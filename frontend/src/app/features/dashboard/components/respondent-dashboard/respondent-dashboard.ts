import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';

export interface RespondentSurvey {
  id: number;
  title: string;
  description: string;
  organizationName: string;
}

@Component({
  selector: 'app-respondent-dashboard',
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
    FormsModule
  ],
  templateUrl: './respondent-dashboard.html',
  styleUrl: './respondent-dashboard.scss',
})
export class RespondentDashboard {
  // Symulacja: Backend zwrócił tylko AKTYWNE i WEWNĘTRZNE ankiety dla pracownika Orlenu
  searchQuery = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  pageSize = 6;
  pageIndex = 0;

  availableSurveys: RespondentSurvey[] = [
    { id: 1, title: 'Ocena roczna satysfakcji z pracy 2026', description: 'Twoja opinia jest dla nas ważna.', organizationName: 'ORLEN S.A.' },
    { id: 2, title: 'Badanie sprzętu biurowego', description: 'Czy laptopy działają poprawnie?', organizationName: 'ORLEN S.A.' },
    { id: 3, title: 'Badanie komunikacji wewnętrznej', description: 'Oceń nowy intranet firmowy.', organizationName: 'ORLEN S.A.' },
    { id: 4, title: 'Wybór benefitów 2027', description: 'Karta sportowa czy pakiet medyczny?', organizationName: 'ORLEN S.A.' },
  ];

  //Zwraca przefiltrowane i posortowane dane
  get processedSurveys() {
    const result = this.availableSurveys.filter(s => 
      s.title.toLowerCase().includes(this.searchQuery.toLowerCase())
    );

    result.sort((a, b) => {
      const comp = a.title.localeCompare(b.title);
      return this.sortDirection === 'asc' ? comp : -comp;
    });

    return result;
  }

  //  Wycina kawałek tablicy na podstawie aktualnej strony (paginacja)
  get paginatedSurveys() {
    const start = this.pageIndex * this.pageSize;
    return this.processedSurveys.slice(start, start + this.pageSize);
  }

  // Obsługa kliknięcia w paginator
  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }
}
