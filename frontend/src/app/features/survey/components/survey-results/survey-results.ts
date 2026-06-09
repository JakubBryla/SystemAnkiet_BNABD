import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-survey-results',
  imports: [
    FormsModule,
    MatCardModule,
    MatSlideToggleModule,
    MatIconModule,
    BaseChartDirective
  ],
  templateUrl: './survey-results.html',
  styleUrl: './survey-results.scss',
})
export class SurveyResults {
  isDataCleaningEnabled = false;

  // Wykres 1: Kołowy 
  public pieChartType: ChartType = 'pie';
  public pieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: ['Bardzo dobrze', 'Dobrze', 'Przeciętnie', 'Źle'],
    datasets: [{ 
      data: [300, 500, 100, 50],
      backgroundColor: ['#4caf50', '#8bc34a', '#ffeb3b', '#f44336'] 
    }]
  };

  // Wykres 2: Słupkowy
  public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar', number[], string | string[]> = {
    labels: ['Kawa', 'Hot-dog', 'Myjnia', 'Płyn do spryskiwaczy'],
    datasets: [
      { data: [650, 420, 300, 150], label: 'Liczba wyborów', backgroundColor: '#1a73e8' }
    ]
  };

  // Opcje dla wykresu słupkowego, żeby zawsze zaczynał się od zera
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: { y: { beginAtZero: true } }
  };

  rawTextAnswers = [
    'Super obsługa, polecam!', 
    'asdfghjkl', // Śmieciowa odpowiedź do odfiltrowania
    'Brakowało mojego ulubionego hot-doga wege.',
    'nie wiem', // Kolejny śmieć
    'Czyste toalety, ale kawa mogłaby być tańsza.'
  ];
  cleanTextAnswers = [
    'Super obsługa, polecam!', 
    'Brakowało mojego ulubionego hot-doga wege.',
    'Czyste toalety, ale kawa mogłaby być tańsza.'
  ];
  
  // Zmienna, którą wyświetlamy w HTML
  currentTextAnswers = this.rawTextAnswers;

  // Funkcja wywoływana przy kliknięciu przełącznika "Data Cleaning"
  toggleDataCleaning() {
    if (this.isDataCleaningEnabled) {
      this.pieChartData.datasets[0].data = [290, 480, 80, 20];
      this.barChartData.datasets[0].data = [600, 400, 250, 100];
      // Podmieniamy na wyczyszczone teksty
      this.currentTextAnswers = this.cleanTextAnswers;
    } else {
      this.pieChartData.datasets[0].data = [300, 500, 100, 50];
      this.barChartData.datasets[0].data = [650, 420, 300, 150];
      // Przywracamy śmieciowe komentarze
      this.currentTextAnswers = this.rawTextAnswers;
    }
    
    this.pieChartData = { ...this.pieChartData };
    this.barChartData = { ...this.barChartData };
  }
}