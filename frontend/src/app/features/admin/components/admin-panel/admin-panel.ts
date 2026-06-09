import { Component, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'RESPONDENT' | 'SURVEYOR' | 'ADMIN';
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
    MatPaginatorModule
  ],
  templateUrl: './admin-panel.html',
  styleUrl: './admin-panel.scss'
})
export class AdminPanel {
  
  users = signal<User[]>([
    { id: 1, firstName: 'Jan', lastName: 'Kowalski', email: 'jan.kowalski@nazwafirmy.pl', role: 'RESPONDENT' },
    { id: 2, firstName: 'Anna', lastName: 'Nowak', email: 'anna.nowak@nazwafirmy.pl', role: 'SURVEYOR' },
    { id: 3, firstName: 'Michał', lastName: 'Szef', email: 'michal.szef@nazwafirmy.pl', role: 'ADMIN' },
    { id: 4, firstName: 'Piotr', lastName: 'Zieliński', email: 'piotr.zielinski@nazwafirmy.pl', role: 'RESPONDENT' },
    { id: 5, firstName: 'Katarzyna', lastName: 'Wiśniewska', email: 'katarzyna.wisniewska@nazwafirmy.pl', role: 'RESPONDENT' },
    { id: 6, firstName: 'Adam', lastName: 'Krawczyk', email: 'adam.krawczyk@nazwafirmy.pl', role: 'SURVEYOR' },
    { id: 7, firstName: 'Ewa', lastName: 'Lis', email: 'ewa.lis@nazwafirmy.pl', role: 'RESPONDENT' }
  ]);

  // --- STANY FILTRÓW I PAGINACJI ---
  searchQuery = '';
  filterRole: 'all' | 'RESPONDENT' | 'SURVEYOR' | 'ADMIN' = 'all';
  sortBy: 'lastName' | 'email' | 'role' = 'lastName';
  sortDirection: 'asc' | 'desc' = 'asc';
  
  pageIndex = 0;
  pageSize = 5;

  // --- PRZETWARZANIE DANYCH ---
  get processedUsers() {
    const filtered = this.users().filter(u => {
      const fullName = `${u.firstName} ${u.lastName}`.toLowerCase();
      const matchSearch = fullName.includes(this.searchQuery.toLowerCase()) || 
                          u.email.toLowerCase().includes(this.searchQuery.toLowerCase());
      
      const matchRole = this.filterRole === 'all' || u.role === this.filterRole;
      
      return matchSearch && matchRole;
    });

    return [...filtered].sort((a, b) => {
      let comp = 0;
      if (this.sortBy === 'lastName') {
        comp = a.lastName.localeCompare(b.lastName);
      } else if (this.sortBy === 'email') {
        comp = a.email.localeCompare(b.email);
      } else if (this.sortBy === 'role') {
        comp = a.role.localeCompare(b.role);
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
  toggleSurveyorRole(userId: number) {
    this.users.update(currentUsers => 
      currentUsers.map(user => {
        if (user.id === userId && user.role !== 'ADMIN') {
          const newRole = user.role === 'SURVEYOR' ? 'RESPONDENT' : 'SURVEYOR';
          return { ...user, role: newRole };
        }
        return user;
      })
    );
  }
}