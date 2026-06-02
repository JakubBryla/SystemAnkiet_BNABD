import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SurveyCreator } from './survey-creator';

describe('SurveyCreator', () => {
  let component: SurveyCreator;
  let fixture: ComponentFixture<SurveyCreator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SurveyCreator],
    }).compileComponents();

    fixture = TestBed.createComponent(SurveyCreator);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
