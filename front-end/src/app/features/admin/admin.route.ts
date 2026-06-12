import { Routes } from '@angular/router';
import { AdminLayoutComponent } from '../../layouts/admin-layout/admin-layout.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component').then(m => m.AdminDashboardComponent),
      },
      {
        path: 'employes',
        loadComponent: () => import('./employes/employes.component').then(m => m.AdminEmployesComponent),
      },
      {
        path: 'departements',
        loadComponent: () => import('./departements/departements.component').then(m => m.AdminDepartementsComponent),
      },
      {
        path: 'jour-ferie',
        loadComponent: () => import('./jour-ferie/jour-ferie.component').then(m => m.AdminJourFerieComponent),
      },
      {
        path: 'regle',
        loadComponent: () => import('./regle/regle.component').then(m => m.AdminRegleComponent),
      },
      {
        path: 'profil',
        loadComponent: () => import('./profil/profil.component').then(m => m.AdminProfilComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'employe', redirectTo: 'employes', pathMatch: 'full' },
      { path: 'departement', redirectTo: 'departements', pathMatch: 'full' },
      { path: 'jours-feries', redirectTo: 'jour-ferie', pathMatch: 'full' },
      { path: 'regles-conge', redirectTo: 'regle', pathMatch: 'full' },
    ],
  },
];
