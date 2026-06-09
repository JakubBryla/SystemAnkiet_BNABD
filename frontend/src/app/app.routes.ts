import { Routes } from '@angular/router';
import { Login } from './features/auth/components/login/login';
import { Register } from './features/auth/components/register/register';
import { Dashboard } from './features/dashboard/components/dashboard/dashboard';
import { SurveyFiller } from './features/survey/components/survey-filler/survey-filler';
import { SurveyCreator } from './features/survey/components/survey-creator/survey-creator';
import { authGuard } from './features/auth/guards/auth-guard';
import { RespondentDashboard } from './features/dashboard/components/respondent-dashboard/respondent-dashboard';
import { SurveyResults } from './features/survey/components/survey-results/survey-results';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { 
    path: 'dashboard', 
    component: Dashboard,
    canActivate: [authGuard] 
  },
  { path: 'my-surveys', component: RespondentDashboard },
  { 
    path: 'survey/:id/edit', 
    component: SurveyCreator,
    canActivate: [authGuard] 
  },
  {
    path: 's/:id',
    component: SurveyFiller
  },
  {
    path: 'survey/:id/results',
    component: SurveyResults
  }
  
];
