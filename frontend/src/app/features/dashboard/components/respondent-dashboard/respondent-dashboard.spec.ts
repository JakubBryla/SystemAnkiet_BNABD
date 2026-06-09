import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RespondentDashboard } from './respondent-dashboard';

describe('RespondentDashboard', () => {
  let component: RespondentDashboard;
  let fixture: ComponentFixture<RespondentDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RespondentDashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(RespondentDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
