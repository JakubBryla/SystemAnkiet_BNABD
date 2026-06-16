import { Component, inject, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

export interface User {
  id: number;
  email: string;
  firstName: string | null;
  lastName: string | null;
  role: 'USER' | 'SURVEYOR' | 'ADMIN';
  domain: string | null;
  active: boolean;
}

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    NgClass,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    BaseChartDirective
  ],
  templateUrl: './admin-panel.html',
  styleUrl: './admin-panel.scss'
})
export class AdminPanel implements OnInit {
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = 'http://localhost:8080/api/users';

  users = signal<User[]>([]);
  isLoading = true;
  errorMessage: string | null = null;

  // --- STANY FILTRÓW I PAGINACJI ---
  searchQuery = '';
  filterRole: 'all' | 'USER' | 'SURVEYOR' | 'ADMIN' = 'all';
  sortBy: 'name' | 'email' | 'role' | 'domain' = 'email';
  sortDirection: 'asc' | 'desc' = 'asc';

  pageIndex = 0;
  pageSize = 5;

  // mock data dla wykresów, w przyszłości można pobierać z API
  userStatsData: ChartData<'bar'> = { labels: [], datasets: [] };
  surveyStatsData: ChartData<'bar'> = { labels: [], datasets: [] };

  public chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    scales: { 
      y: { beginAtZero: true, ticks: { stepSize: 5 } } 
    }
  };
  
  public chartType: ChartType = 'bar';

  ngOnInit() {
    this.loadUsers();
    this.loadMockStatistics();
  }

  // przykładowe dane dla wykresów - w przyszłości można pobierać z API
  private loadMockStatistics() {
    const timeline = this.generateLast12Months();
    const labels = timeline.map(t => t.label);

    // Sztywne przykładowe dane dla 12 słupków (od 11 miesięcy temu do dziś)
    const mockUserCounts = [4, 7, 12, 9, 15, 22, 19, 25, 34, 28, 40, 48];
    const mockSurveyCounts = [2, 5, 8, 4, 11, 14, 10, 18, 22, 17, 29, 35];

    this.userStatsData = {
      labels,
      datasets: [
        { data: mockUserCounts, label: 'Nowi użytkownicy', backgroundColor: '#1a73e8' }
      ]
    };

    this.surveyStatsData = {
      labels,
      datasets: [
        { data: mockSurveyCounts, label: 'Utworzone ankiety', backgroundColor: '#34a853' }
      ]
    };
  }

  private generateLast12Months(): { label: string }[] {
    const months = [];
    const now = new Date();
    
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push({
        label: `${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`
      });
    }
    return months;
  }

  private loadUsers() {
    this.isLoading = true;
    this.errorMessage = null;

    this.http.get<User[]>(this.apiUrl).subscribe({
      next: (users) => {
        this.users.set(users);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Nie można załadować listy użytkowników.';
        this.cdr.detectChanges();
      }
    });
  }

  /** Zwraca czytelną nazwę użytkownika: imię + nazwisko lub sam email */
  displayName(user: User): string {
    const fn = user.firstName?.trim() ?? '';
    const ln = user.lastName?.trim() ?? '';
    return (fn || ln) ? `${fn} ${ln}`.trim() : user.email;
  }

  /** Czytelna etykieta roli */
  roleLabel(role: 'USER' | 'SURVEYOR' | 'ADMIN'): string {
    if (role === 'ADMIN') return 'Administrator';
    if (role === 'SURVEYOR') return 'Ankieter';
    return 'Użytkownik';
  }

  // --- PRZETWARZANIE DANYCH ---
  get processedUsers() {
    const q = this.searchQuery.toLowerCase();
    const filtered = this.users().filter(u => {
      const name = this.displayName(u).toLowerCase();
      const matchSearch = name.includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.domain ?? '').toLowerCase().includes(q);
      const matchRole = this.filterRole === 'all' || u.role === this.filterRole;
      return matchSearch && matchRole;
    });

    return [...filtered].sort((a, b) => {
      let comp = 0;
      if (this.sortBy === 'name') {
        comp = this.displayName(a).localeCompare(this.displayName(b));
      } else if (this.sortBy === 'email') {
        comp = (a.email ?? '').localeCompare(b.email ?? '');
      } else if (this.sortBy === 'role') {
        comp = (a.role ?? '').localeCompare(b.role ?? '');
      } else if (this.sortBy === 'domain') {
        comp = (a.domain ?? '').localeCompare(b.domain ?? '');
      }
      return this.sortDirection === 'asc' ? comp : -comp;
    });
  }

  get paginatedUsers() {
    const start = this.pageIndex * this.pageSize;
    return this.processedUsers.slice(start, start + this.pageSize);
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  // --- AKCJE ---
  setRole(userId: number, newRole: 'USER' | 'SURVEYOR' | 'ADMIN') {
    this.http.patch<User>(`${this.apiUrl}/${userId}/role`, { role: newRole }).subscribe({
      next: (updated) => {
        this.users.update(list => list.map(u => u.id === userId ? updated : u));
        this.snackBar.open(`Rola zmieniona na: ${this.roleLabel(updated.role)}`, 'OK', { duration: 3000 });
        this.cdr.detectChanges();

        const maxPage = Math.max(0, Math.ceil(this.processedUsers.length / this.pageSize) - 1);
        if (this.pageIndex > maxPage) {
          this.pageIndex = maxPage;
        }
      },
      error: (err) => {
        const msg = err.error?.message || err.error?.error || 'Nie można zmienić roli.';
        this.snackBar.open(msg, 'OK', { duration: 4000 });
      }
    });
  }
}
