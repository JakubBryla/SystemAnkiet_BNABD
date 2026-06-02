import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteSurveyDialog } from './delete-survey-dialog';

describe('DeleteSurveyDialog', () => {
  let component: DeleteSurveyDialog;
  let fixture: ComponentFixture<DeleteSurveyDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteSurveyDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteSurveyDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
