import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { SurveyFiller } from './survey-filler';

describe('SurveyFiller', () => {
  let component: SurveyFiller;
  let fixture: ComponentFixture<SurveyFiller>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SurveyFiller],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: 'test-survey' }),
            },
          },
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SurveyFiller);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
