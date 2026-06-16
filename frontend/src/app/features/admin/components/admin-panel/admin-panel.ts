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

export interface MonthlyStat {
  label: string;
  count: number;
}

export interface AdminStatistics {
  userStats: MonthlyStat[];
  surveyStats: MonthlyStat[];
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
  private statisticsUrl = 'http://localhost:8080/api/admin/statistics';

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

  // dane wykresów ładowane z GET /api/admin/statistics
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
    this.loadStatistics();
  }

  // Pobiera liczbę nowych użytkowników i utworzonych ankiet z ostatnich 12 miesięcy z backendu
  private loadStatistics() {
    this.http.get<AdminStatistics>(this.statisticsUrl).subscribe({
      next: (stats) => {
        this.userStatsData = {
          labels: stats.userStats.map(s => s.label),
          datasets: [
            { data: stats.userStats.map(s => s.count), label: 'Nowi użytkownicy', backgroundColor: '#1a73e8' }
          ]
        };

        this.surveyStatsData = {
          labels: stats.surveyStats.map(s => s.label),
          datasets: [
            { data: stats.surveyStats.map(s => s.count), label: 'Utworzone ankiety', backgroundColor: '#34a853' }
          ]
        };
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Błąd ładowania statystyk:', err)
    });
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
