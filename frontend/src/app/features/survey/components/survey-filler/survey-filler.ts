import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule, MatCheckboxChange } from '@angular/material/checkbox';

export interface Question {
  id: number;
  text: string;
  type: 'short-answer' | 'single-choice' | 'multiple-choice';
  options: string[];
  isRequired: boolean;
}

export interface Survey {
  id: string; 
  title: string;
  description: string;
  status: 'active' | 'closed' | 'draft';
  questions: Question[];
}

@Component({
  selector: 'app-survey-filler',
  imports: [
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
  private fb = inject(FormBuilder);

  survey: Survey | null = null;
  viewState: 'loading' | 'active' | 'closed' | 'draft' | 'not-found' = 'loading';

  answersForm!: FormGroup;

  ngOnInit() {
    const surveyId = this.route.snapshot.paramMap.get('id')!;

    // w prawdziwej aplikacji tutaj byśmy pobierali dane z backendu, ale na potrzeby tego demo zbudujemy przykładową ankietę "na sztywno"
    this.survey = {
      id: surveyId,
      title: 'Badanie satysfakcji z pracy w IT',
      description: 'Twoje odpowiedzi są w pełni anonimowe. Dziękujemy za poświęcony czas!',
      status: 'active', // draft, active, closed
      questions: [
        // Pierwsze pytanie: WYMAGANE
        { id: 101, text: 'Na jakim stanowisku pracujesz?', type: 'short-answer', options: [], isRequired: true },
        // Drugie pytanie: OPCJONALNE
        { id: 102, text: 'Jaki jest Twój ulubiony framework?', type: 'single-choice', options: ['Angular', 'React', 'Vue'], isRequired: false },
        { id: 103, text: 'Z jakich narzędzi korzystasz na co dzień?', type: 'multiple-choice', options: ['VS Code', 'Git', 'Docker', 'Jira'], isRequired: true }
      ]
    };

    this.viewState = this.survey.status; 
    if (this.viewState === 'active') {
      this.buildForm();
    }
  }

  private buildForm() {
    this.answersForm = this.fb.group({});

    this.survey?.questions.forEach(question => {
      const validators = question.isRequired ? [Validators.required] : [];
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
  
  submitAnswers() {
    if (this.answersForm.valid) {
      console.log('WYSYŁAMY ODPOWIEDZI NA BACKEND');
      console.log('ID Ankiety:', this.survey?.id);
      console.log('Udzielone odpowiedzi:', this.answersForm.value);
      
      alert('Dziękujemy! Odpowiedzi zostały zapisane.');
      
      this.viewState = 'closed';
    }
  }
}
