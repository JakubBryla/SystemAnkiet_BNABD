import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

interface AnswerDetail {
  questionId: number;
  questionText: string;
  questionType: string;
  answerValue: string;
}

interface ResponseDetail {
  id: number;
  submittedAt: string;
  flagged: boolean;
  flagStatus: string | null;
  flagReason: string | null;
  answers: AnswerDetail[];
}

interface QuestionStats {
  id: number;
  text: string;
  type: string;
  // Dla pytań z opcjami: liczba wyborów każdej opcji
  counts: Record<string, number>;
  // Dla pytań tekstowych: lista odpowiedzi
  textAnswers: string[];
  totalAnswers: number;
}

@Component({
  selector: 'app-survey-results',
  imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    RouterModule,
    BaseChartDirective
  ],
  templateUrl: './survey-results.html',
  styleUrl: './survey-results.scss',
})
export class SurveyResults implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = 'http://localhost:8080/api/surveys';
  private surveyId!: string;

  surveyTitle = '';
  isLoading = true;
  errorMessage: string | null = null;

  allResponses: ResponseDetail[] = [];
  isDataCleaningEnabled = false;

  questionStats: QuestionStats[] = [];

  // Kolory dla wykresów
  private chartColors = [
    '#1a73e8', '#34a853', '#fbbc04', '#ea4335',
    '#9c27b0', '#ff5722', '#00bcd4', '#607d8b'
  ];

  ngOnInit() {
    this.surveyId = this.route.snapshot.paramMap.get('id')!;
    this.loadData();
  }

  private loadData() {
    this.isLoading = true;
    this.errorMessage = null;

    // Ładujemy survey info + odpowiedzi równolegle
    this.http.get<any>(`${this.apiUrl}/${this.surveyId}/public`).subscribe({
      next: (survey) => {
        this.surveyTitle = survey.title;
        this.loadResponses();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Nie można załadować danych ankiety.';
        this.cdr.detectChanges();
      }
    });
  }

  private loadResponses() {
    this.http.get<ResponseDetail[]>(`${this.apiUrl}/${this.surveyId}/responses`).subscribe({
      next: (responses) => {
        this.allResponses = responses;
        this.buildStats();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Nie można załadować odpowiedzi.';
        this.cdr.detectChanges();
      }
    });
  }

  private buildStats() {
    const responses = this.isDataCleaningEnabled
      ? this.allResponses.filter(r => !r.flagged)
      : this.allResponses;

    // Zbierz unikalne pytania z pierwszej odpowiedzi (kolejność zachowana)
    const questionMap = new Map<number, QuestionStats>();

    for (const response of responses) {
      for (const answer of response.answers) {
        if (!questionMap.has(answer.questionId)) {
          questionMap.set(answer.questionId, {
            id: answer.questionId,
            text: answer.questionText,
            type: answer.questionType,
            counts: {},
            textAnswers: [],
            totalAnswers: 0
          });
        }

        const stats = questionMap.get(answer.questionId)!;
        if (!answer.answerValue) continue;

        if (answer.questionType === 'short-answer') {
          stats.textAnswers.push(answer.answerValue);
        } else if (answer.questionType === 'multiple-choice') {
          // Odpowiedź przechowywana jako "Opcja A, Opcja B"
          const parts = answer.answerValue.split(',').map(s => s.trim()).filter(s => s);
          for (const part of parts) {
            stats.counts[part] = (stats.counts[part] ?? 0) + 1;
          }
        } else {
          // single-choice
          stats.counts[answer.answerValue] = (stats.counts[answer.answerValue] ?? 0) + 1;
        }
        stats.totalAnswers++;
      }
    }

    this.questionStats = Array.from(questionMap.values());
  }

  get displayedResponses(): ResponseDetail[] {
    return this.isDataCleaningEnabled
      ? this.allResponses.filter(r => !r.flagged)
      : this.allResponses;
  }

  get flaggedCount(): number {
    return this.allResponses.filter(r => r.flagged).length;
  }

  toggleDataCleaning() {
    this.buildStats();
    this.cdr.detectChanges();
  }

  // Buduje ChartData dla pytania single-choice (pie)
  getPieData(stats: QuestionStats): ChartData<'pie', number[], string> {
    const labels = Object.keys(stats.counts);
    return {
      labels,
      datasets: [{
        data: labels.map(l => stats.counts[l]),
        backgroundColor: labels.map((_, i) => this.chartColors[i % this.chartColors.length])
      }]
    };
  }

  // Buduje ChartData dla pytania multiple-choice lub single-choice (bar)
  getBarData(stats: QuestionStats): ChartData<'bar', number[], string> {
    const labels = Object.keys(stats.counts);
    return {
      labels,
      datasets: [{
        data: labels.map(l => stats.counts[l]),
        label: 'Liczba wyborów',
        backgroundColor: this.chartColors[0]
      }]
    };
  }

  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
  };

  public pieChartType: ChartType = 'pie';
  public barChartType: ChartType = 'bar';
}
