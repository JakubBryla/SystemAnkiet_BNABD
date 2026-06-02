import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SurveyFiller } from './survey-filler';

describe('SurveyFiller', () => {
  let component: SurveyFiller;
  let fixture: ComponentFixture<SurveyFiller>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SurveyFiller],
    }).compileComponents();

    fixture = TestBed.createComponent(SurveyFiller);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
