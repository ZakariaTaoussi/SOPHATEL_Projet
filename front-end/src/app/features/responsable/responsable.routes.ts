import { Routes } from '@angular/router';
import { ResponsableLayoutComponent } from '../../layouts/responsable-layout/responsable-layout.component';

export const RESPONSABLE_ROUTES: Routes = [
	{
		path: '',
		component: ResponsableLayoutComponent,
		children: [
			{
				path: 'dashboard',
				loadComponent: () => import('./dashboard/dashboard.component').then(m => m.ResponsableDashboardComponent),
			},
			{
				path: 'mes-absences',
				loadComponent: () => import('./mes-absences/mes-absences.component').then(m => m.ResponsableMesAbsencesComponent),
			},
			{
				path: 'mes-demandes',
				loadComponent: () => import('./mes-demandes/mes-demandes.component').then(m => m.ResponsableMesDemandesComponent),
			},
			{
				path: 'nouvelle-demande',
				loadComponent: () => import('./nouvelle-demande/nouvelle-demande.component').then(m => m.ResponsableNouvelleDemandeComponent),
			},
			{
				path: 'historique',
				loadComponent: () => import('./historique/historique.component').then(m => m.ResponsableHistoriqueComponent),
			},
			{
				path: 'mes-employes',
				loadComponent: () => import('./mes-employes/mes-employes.component').then(m => m.ResponsableMesEmployesComponent),
			},
			{
				path: 'notifications',
				loadComponent: () => import('./notifications/notifications.component').then(m => m.ResponsableNotificationsComponent),
			},
			{
				path: 'profil',
				loadComponent: () => import('./profil/profil.component').then(m => m.ResponsableProfilComponent),
			},
			{
				path: 'demandes-a-valider',
				loadComponent: () => import('./validation-demandes/validation-demandes.component').then(m => m.ResponsableValidationDemandesComponent),
			},
			{
				path: 'validation-demandes',
				loadComponent: () => import('./validation-demandes/validation-demandes.component').then(m => m.ResponsableValidationDemandesComponent),
			},
			{ path: '', redirectTo: 'dashboard', pathMatch: 'full' },
		],
	},
];
