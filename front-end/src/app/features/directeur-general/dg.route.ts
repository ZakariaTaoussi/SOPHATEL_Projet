import { Routes } from '@angular/router';
import { DirecteurGeneralLayoutComponent } from '../../layouts/directeur-general-layout/directeur-general-layout.component';
import { Role } from '../../core/enums/role.enum';

export const DIRECTEUR_GENERAL_ROUTES: Routes = [
  {
    path: '',
    component: DirecteurGeneralLayoutComponent,
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DirecteurGeneralDashboardComponent),
      },
      {
        path: 'employes',
        loadComponent: () => import('./employe/employe.component').then(m => m.DirecteurGeneralEmployeComponent),
      },
      {
        path: 'employe',
        redirectTo: 'employes',
        pathMatch: 'full',
      },
      {
        path: 'demande-employe',
        loadComponent: () => import('./demande-employe/demande-employe.component').then(m => m.DirecteurGeneralDemandeEmployeComponent),
      },
      {
        path: 'demandes-a-valider',
        loadComponent: () => import('./demande-employe/demande-employe.component').then(m => m.DirecteurGeneralDemandeEmployeComponent),
      },
      {
        path: 'absences-a-valider',
        loadComponent: () => import('./demande-employe/demande-employe.component').then(m => m.DirecteurGeneralDemandeEmployeComponent),
      },
      {
        path: 'demandes-validees',
        loadComponent: () => import('./demande-employe/demande-employe.component').then(m => m.DirecteurGeneralDemandeEmployeComponent),
      },
      {
        path: 'mes-demandes',
        data: { scope: 'directeur-general', baseRoute: '/directeur-general' },
        loadComponent: () => import('../shared/self-demande/mes-demandes.component').then(m => m.SelfMesDemandesComponent),
      },
      {
        path: 'nouvelle-demande',
        data: { scope: 'directeur-general', baseRoute: '/directeur-general' },
        loadComponent: () => import('../shared/self-demande/nouvelle-demande.component').then(m => m.SelfNouvelleDemandeComponent),
      },
      {
        path: 'historique',
        loadComponent: () => import('./historique/historique.component').then(m => m.DirecteurGeneralHistoriqueComponent),
      },
      {
        path: 'notifications',
        loadComponent: () => import('../shared/notification-page/notification-page.component').then(m => m.SharedNotificationPageComponent),
      },
      {
        path: 'profil',
        data: { role: Role.DIRECTEUR_GENERAL },
        loadComponent: () => import('../shared/profil/profil.component').then(m => m.SharedProfilComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
