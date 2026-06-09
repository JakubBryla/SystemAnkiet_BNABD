import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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
  
  surveyForm!: FormGroup;

  ngOnInit() {
    // Kiedy komponent się ładuje, budujemy szkielet formularza
    this.surveyForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      accessType: ['EXTERNAL', Validators.required],
      questions: this.fb.array([], atLeastOneQuestion) 
    });

    // puste pytanie, żeby użytkownik miał od czego zacząć
    this.addQuestion();
    const surveyId = this.route.snapshot.paramMap.get('id');

    if (surveyId) {
      this.surveyForm.patchValue({
        // symulacja tytułu (docelowo pobierzemy go z bazy przez API)
        title: `Edycja ankiety nr ${surveyId}`
      });
    }
  }

  get questions() {
    return this.surveyForm.get('questions') as FormArray;
  }

  getOptions(questionIndex: number) {
    return this.questions.at(questionIndex).get('options') as FormArray;
  }

  addQuestion() {
    const questionForm = this.fb.group({
      text: ['', Validators.required], 
      type: ['short-answer', Validators.required],
      isRequired: [true],
      options: this.fb.array([]),
      isControlQuestion: [false],
      expectedValue: [''] 
    }, { validators: optionsValidator });
    
    questionForm.get('isControlQuestion')?.valueChanges.subscribe(isControl => {
      const expectedValueControl = questionForm.get('expectedValue');
      if (isControl) {
        // włączone -> odpowiedź absolutnie wymagana
        expectedValueControl?.setValidators([Validators.required]);
      } else {
        // wyłączone -> zdejmujemy wymóg i ewentualnie czyścimy pole
        expectedValueControl?.clearValidators();
        expectedValueControl?.setValue('');
      }
      expectedValueControl?.updateValueAndValidity();
    });

    this.questions.push(questionForm);
  }

  removeQuestion(index: number) {
    this.questions.removeAt(index);
  }

  addOption(questionIndex: number) {
    const optionControl = this.fb.control('', Validators.required);
    this.getOptions(questionIndex).push(optionControl);
  }

  removeOption(questionIndex: number, optionIndex: number) {
    this.getOptions(questionIndex).removeAt(optionIndex);
  }

  saveSurvey() {
    if (this.surveyForm.valid) {
      const finalSurveyData = this.surveyForm.value;
      
      console.log('GOTOWA ANKIETA DO WYSŁANIA NA BACKEND');
      console.log(finalSurveyData);
      
      // w przyszłości tutaj wywołamy serwis, który wyśle dane do backendu. Na razie tylko alert i przekierowanie
      
      alert('Ankieta została zapisana pomyślnie!');
      
      this.router.navigate(['/dashboard']);
    }
  }

  
}
