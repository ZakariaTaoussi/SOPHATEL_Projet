import { Routes } from '@angular/router';
import { DirecteurGeneralLayoutComponent } from '../../layouts/directeur-general-layout/directeur-general-layout.component';

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
        path: 'employe',
        loadComponent: () => import('./employe/employe.component').then(m => m.DirecteurGeneralEmployeComponent),
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
        path: 'profil',
        loadComponent: () => import('./profil/profil.component').then(m => m.DirecteurGeneralProfilComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
