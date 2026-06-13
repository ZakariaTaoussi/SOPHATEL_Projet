import { Routes } from '@angular/router';
import { loginGuard } from '../../core/guards/login.guard';
import { LoginComponent } from './login/login.component';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    canActivate: [loginGuard],
    component: LoginComponent
  }
];
