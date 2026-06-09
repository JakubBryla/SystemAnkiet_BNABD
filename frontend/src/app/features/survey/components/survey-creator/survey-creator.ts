import { Component, inject, OnInit, DestroyRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatRadioModule } from '@angular/material/radio';
import { MatDividerModule } from '@angular/material/divider';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export function atLeastOneQuestion(control: AbstractControl): ValidationErrors | null {
  return control.value && control.value.length > 0 ? null : { requireQuestion: true };
}

export const optionsValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const type = control.get('type')?.value;
  const options = control.get('options') as FormArray;
  if ((type === 'single-choice' || type === 'multiple-choice') && options.length === 0) {
    return { noOptions: true };
  }
  return null;
};

@Component({
  selector: 'app-survey-creator',
  imports: [
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatSelectModule,
    MatSlideToggleModule,
    ReactiveFormsModule,
    MatRadioModule,
    MatDividerModule,
    FormsModule
  ],
  templateUrl: './survey-creator.html',
  styleUrl: './survey-creator.scss',
})
export class SurveyCreator implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  private apiUrl = 'http://localhost:8080/api/surveys';
  private surveyId: string | null = null;

  surveyForm!: FormGroup;
  isSaving = false;
  saveError: string | null = null;

  ngOnInit() {
    this.surveyForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      accessType: ['EXTERNAL', Validators.required],
      questions: this.fb.array([], atLeastOneQuestion)
    });

    this.surveyId = this.route.snapshot.paramMap.get('id');

    if (this.surveyId) {
      // Wczytaj istniejącą ankietę z backendu
      this.http.get<any>(`${this.apiUrl}/${this.surveyId}/public`).subscribe({
        next: (data) => {
          this.surveyForm.patchValue({
            title: data.title,
            description: data.description ?? '',
            accessType: (data.type ?? 'external').toUpperCase()
          });

          // Wypełnij pytania z backendu
          this.questions.clear();
          (data.questions ?? []).forEach((q: any) => {
            this.questions.push(this.createQuestionForm(q));
          });

          // Jeśli ankieta nie ma jeszcze żadnych pytań — dodaj jedno puste
          if (this.questions.length === 0) {
            this.addQuestion();
          }
        },
        error: (err) => {
          console.error('Błąd ładowania ankiety do edycji:', err);
          this.addQuestion();
        }
      });
    } else {
      // Nowa ankieta — zacznij od jednego pustego pytania
      this.addQuestion();
    }
  }

  get questions() {
    return this.surveyForm.get('questions') as FormArray;
  }

  getOptions(questionIndex: number) {
    return this.questions.at(questionIndex).get('options') as FormArray;
  }

  // Tworzy FormGroup dla pytania — opcjonalnie wypełniony danymi z backendu
  private createQuestionForm(data?: any): FormGroup {
    const questionForm = this.fb.group({
      text: [data?.text ?? '', Validators.required],
      type: [data?.type ?? 'short-answer', Validators.required],
      isRequired: [data?.isRequired ?? true],
      options: this.fb.array(
        (data?.options ?? []).map((opt: string) =>
          this.fb.control(opt, Validators.required)
        )
      ),
      isControlQuestion: [data?.isControlQuestion ?? false],
      expectedValue: [data?.expectedValue ?? '']
    }, { validators: optionsValidator });

    // Walidator expectedValue aktywny tylko gdy pytanie jest kontrolne
    questionForm.get('isControlQuestion')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(isControl => {
        const expectedValueControl = questionForm.get('expectedValue');
        if (isControl) {
          expectedValueControl?.setValidators([Validators.required]);
        } else {
          expectedValueControl?.clearValidators();
          expectedValueControl?.setValue('');
        }
        expectedValueControl?.updateValueAndValidity();
      });

    return questionForm;
  }

  addQuestion() {
    this.questions.push(this.createQuestionForm());
  }

  removeQuestion(index: number) {
    this.questions.removeAt(index);
  }

  addOption(questionIndex: number) {
    const optionControl = this.fb.control('', Validators.required);
    this.getOptions(questionIndex).push(optionControl);
  }

  removeOption(qIndex: number, optIndex: number) {
    const question = this.questions.at(qIndex);
    const options = question.get('options') as FormArray;
    const expectedValueCtrl = question.get('expectedValue');
    const removedValue = options.at(optIndex).value;

    if (expectedValueCtrl?.value === removedValue) {
      expectedValueCtrl?.setValue('');
    }

    options.removeAt(optIndex);
  }

  saveSurvey() {
    if (this.surveyForm.invalid) return;

    this.isSaving = true;
    this.saveError = null;

    const formValue = this.surveyForm.value;

    // Mapowanie formularza na format backendu
    const payload = {
      title: formValue.title.trim(),
      description: formValue.description ?? '',
      type: formValue.accessType,  // accessType (frontend) → type (backend)
      questions: (formValue.questions ?? []).map((q: any) => ({
        text: q.text,
        type: q.type,
        isRequired: q.isRequired,
        options: q.options ?? [],
        isControlQuestion: q.isControlQuestion ?? false,
        expectedValue: q.expectedValue || null,
        failStatus: null
      }))
    };

    const request$ = this.surveyId
      ? this.http.put<any>(`${this.apiUrl}/${this.surveyId}`, payload)
      : this.http.post<any>(this.apiUrl, payload);

    request$.subscribe({
      next: () => {
        this.isSaving = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isSaving = false;
        this.saveError = err.error?.error || 'Błąd podczas zapisywania ankiety.';
        console.error('Błąd zapisywania:', err);
      }
    });
  }
}
