import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateSurveyDialog } from './create-survey-dialog';

describe('CreateSurveyDialog', () => {
  let component: CreateSurveyDialog;
  let fixture: ComponentFixture<CreateSurveyDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateSurveyDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateSurveyDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
