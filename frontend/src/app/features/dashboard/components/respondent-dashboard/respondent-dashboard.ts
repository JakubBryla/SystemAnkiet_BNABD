import { Component } from '@angular/core';
import { NgClass } from '@angular/common'; 
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
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
    NgClass,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './respondent-dashboard.html',
  styleUrl: './respondent-dashboard.scss',
})
export class RespondentDashboard {
  // Symulacja: Backend zwrócił tylko AKTYWNE i WEWNĘTRZNE ankiety dla pracownika Orlenu
  availableSurveys: RespondentSurvey[] = [
    { 
      id: 1, 
      title: 'Ocena roczna satysfakcji z pracy 2026', 
      description: 'Twoja opinia jest dla nas ważna. Wypełnij anonimową ankietę pracowniczą.', 
      organizationName: 'ORLEN S.A.'
    },
    { 
      id: 3, 
      title: 'Badanie komunikacji wewnętrznej', 
      description: 'Oceń nowy intranet firmowy oraz przepływ informacji w zespole.', 
      organizationName: 'ORLEN S.A.'
    }
  ];
}
