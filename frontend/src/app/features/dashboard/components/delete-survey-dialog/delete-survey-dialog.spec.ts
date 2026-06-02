import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

import { DeleteSurveyDialog } from './delete-survey-dialog';

describe('DeleteSurveyDialog', () => {
  let component: DeleteSurveyDialog;
  let fixture: ComponentFixture<DeleteSurveyDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteSurveyDialog],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {title: 'Testowa ankieta'}
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteSurveyDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
