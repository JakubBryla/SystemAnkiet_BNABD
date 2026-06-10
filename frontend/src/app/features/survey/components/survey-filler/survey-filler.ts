import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { timeout, TimeoutError } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule, MatCheckboxChange } from '@angular/material/checkbox';

export function trimRequiredValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const isWhitespace = (control.value || '').trim().length === 0;
    return isWhitespace ? { required: true } : null;
  };
}

export interface Question {
  id: number;
  text: string;
  type: 'short-answer' | 'single-choice' | 'multiple-choice';
  options: string[];
  isRequired: boolean;
  isControlQuestion?: boolean;
  expectedValue?: string;
}

export interface Survey {
  id: string;
  title: string;
  description: string;
  status: 'active' | 'closed' | 'draft';
  type: 'internal' | 'external';
  creatorDomain: string | null;
  questions: Question[];
  lastActivatedAt: string | null;
}

@Component({
  selector: 'app-survey-filler',
  imports: [
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatRadioModule,
    MatInputModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatCheckboxModule
  ],
  templateUrl: './survey-filler.html',
  styleUrl: './survey-filler.scss',
})
export class SurveyFiller implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  private apiUrl = 'http://localhost:8080/api/surveys';
  private surveyId!: string;

  survey: Survey | null = null;
  viewState: 'loading' | 'active' | 'closed' | 'draft' | 'not-found' | 'submitted' | 'login-required' | 'access-denied' | 'already-submitted' = 'loading';
  accessDeniedDomain: string | null = null;
  errorMessage: string | null = null;

  answersForm!: FormGroup;

  ngOnInit() {
    this.surveyId = this.route.snapshot.paramMap.get('id')!;

    this.http.get<any>(`${this.apiUrl}/${this.surveyId}/public`)
      .pipe(timeout(10000))
      .subscribe({
        next: (data) => {
          this.survey = {
            id: String(data.id),
            title: data.title,
            description: data.description ?? '',
            status: data.status,
            type: data.type ?? 'external',
            creatorDomain: data.creatorDomain ?? null,
            lastActivatedAt: data.lastActivatedAt ?? null,
            questions: (data.questions ?? []).map((q: any) => ({
              id: q.id,
              text: q.text,
              type: q.type,
              options: q.options ?? [],
              isRequired: q.isRequired ?? false,
              isControlQuestion: q.isControlQuestion ?? false,
              expectedValue: q.expectedValue
            }))
          };

          // Ankieta wewnętrzna — sprawdź logowanie i domenę
          if (this.survey.type === 'internal') {
            const token = localStorage.getItem('token');

            if (!token) {
              // Niezalogowany — zapisz returnUrl i pokaż ekran logowania
              sessionStorage.setItem('loginReturnUrl', `/s/${this.surveyId}`);
              this.viewState = 'login-required';
              this.cdr.detectChanges();
              return;
            }

            // Zalogowany — sprawdź domenę
            const userEmail = localStorage.getItem('email') ?? '';
            const userDomain = userEmail.includes('@') ? userEmail.split('@')[1].toLowerCase() : '';
            const surveyDomain = (this.survey.creatorDomain ?? '').toLowerCase();

            if (surveyDomain && userDomain !== surveyDomain) {
              this.accessDeniedDomain = this.survey.creatorDomain;
              this.viewState = 'access-denied';
              this.cdr.detectChanges();
              return;
            }
          }

          // Sprawdź czy ankieta była już wypełniona w tej przeglądarce.
          // Klucz zawiera email użytkownika i datę ostatniej aktywacji ankiety:
          //  - email: różni użytkownicy na tej samej przeglądarce nie blokują się nawzajem
          //  - lastActivatedAt: po zamknięciu i ponownym otwarciu ankiety klucz się zmienia,
          //    więc poprzednie wypełnienie nie blokuje nowego okresu aktywności
          if (localStorage.getItem(this.getSubmittedKey())) {
            this.viewState = 'already-submitted';
            this.cdr.detectChanges();
            return;
          }

          this.viewState = this.survey.status;
          if (this.viewState === 'active') {
            this.buildForm();
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          if (err instanceof TimeoutError) {
            this.errorMessage = 'Serwer nie odpowiada. Sprawdź czy backend jest uruchomiony.';
          } else if (err.status === 0) {
            this.errorMessage = 'Brak połączenia z serwerem (localhost:8080).';
          } else if (err.status === 404) {
            this.viewState = 'not-found';
            this.cdr.detectChanges();
            return;
          }
          this.viewState = 'not-found';
          console.error('Błąd ładowania ankiety:', err);
          this.cdr.detectChanges();
        }
      });
  }

  private buildForm() {
    this.answersForm = this.fb.group({});

    this.survey?.questions.forEach(question => {
      const validators: ValidatorFn[] = [];

      if (question.isRequired) {
        if (question.type === 'short-answer') {
          validators.push(trimRequiredValidator());
        } else {
          validators.push(Validators.required);
        }
      }

      const initialValue = question.type === 'multiple-choice' ? [] : '';
      this.answersForm.addControl(
        question.id.toString(),
        this.fb.control(initialValue, validators)
      );
    });
  }

  onCheckboxChange(event: MatCheckboxChange, questionId: number, option: string) {
    const control = this.answersForm.get(questionId.toString())!;
    const currentValue = control.value as string[];

    if (event.checked) {
      control.setValue([...currentValue, option]);
    } else {
      control.setValue(currentValue.filter(val => val !== option));
    }

    control.markAsTouched();
  }

  // Buduje unikalny klucz localStorage dla kombinacji: ankieta + użytkownik + okres aktywności.
  // email    → różni użytkownicy na tej samej przeglądarce nie blokują się nawzajem
  // lastActivatedAt → po zamknięciu i ponownym otwarciu ankiety klucz się zmienia,
  //                   dzięki czemu stare wypełnienia nie blokują nowego okresu
  private getSubmittedKey(): string {
    const email = localStorage.getItem('email') ?? 'anonymous';
    const activatedAt = this.survey?.lastActivatedAt ?? '';
    return `survey_submitted_${this.surveyId}_${email}_${activatedAt}`;
  }

  goToLogin() {
    sessionStorage.setItem('loginReturnUrl', `/s/${this.surveyId}`);
    this.router.navigate(['/login']);
  }

  submitAnswers() {
    if (!this.answersForm.valid) return;

    // Zamień wartości formularza na Map<questionId, answerValue>
    // Dla multiple-choice — łączymy tablicę w string rozdzielony przecinkami
    const rawValues = this.answersForm.value as Record<string, any>;
    const answers: Record<string, string> = {};

    for (const [key, value] of Object.entries(rawValues)) {
      if (Array.isArray(value)) {
        answers[key] = value.join(', ');
      } else {
        answers[key] = String(value ?? '');
      }
    }

    this.http.post<any>(`${this.apiUrl}/${this.surveyId}/responses`, { answers }).subscribe({
      next: () => {
        this.errorMessage = null;
        // Zapisz do localStorage żeby zapobiec ponownemu wypełnieniu
        localStorage.setItem(this.getSubmittedKey(), '1');
        this.viewState = 'submitted';
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (err.status === 409) {
          // Ankieta już wypełniona (backend potwierdził dla INTERNAL)
          localStorage.setItem(this.getSubmittedKey(), '1');
          this.viewState = 'already-submitted';
          this.cdr.detectChanges();
          return;
        }
        const msg = err.error?.error || 'Błąd podczas wysyłania odpowiedzi.';
        this.errorMessage = msg;
        console.error('Błąd wysyłania odpowiedzi:', err);
      }
    });
  }
}
