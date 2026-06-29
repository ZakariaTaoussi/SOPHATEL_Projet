import { Routes } from '@angular/router';
import { EmployeLayoutComponent } from '../../layouts/employe-layout/employe-layout.component';
import { Role } from '../../core/enums/role.enum';

export const EMPLOYE_ROUTES: Routes = [
  {
    path: '',
    component: EmployeLayoutComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'mes-absences',
        loadComponent: () =>
          import('./mes-absences/mes-absences.component').then(m => m.MesAbsencesComponent),
      },
      {
        path: 'mes-demandes',
        loadComponent: () =>
          import('./mes-demandes/mes-demandes.component').then(m => m.MesDemandesComponent),
      },
      {
        path: 'nouvelle-demande',
        loadComponent: () =>
          import('./nouvelle-demande/nouvelle-demande.component').then(m => m.NouvellDemandeComponent),
      },
      {
        path: 'notification',
        loadComponent: () =>
          import('../shared/notification-page/notification-page.component').then(m => m.SharedNotificationPageComponent),
      },
      // 'Historique' removed per request; use 'Mes Demandes' with filters instead
      {
        path: 'profil',
        data: { role: Role.EMPLOYE },
        loadComponent: () =>
          import('../shared/profil/profil.component').then(m => m.SharedProfilComponent),
      },
      // Redirect par défaut
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
