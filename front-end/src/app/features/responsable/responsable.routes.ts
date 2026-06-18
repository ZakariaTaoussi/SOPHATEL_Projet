import { Routes } from '@angular/router';
import { ResponsableLayoutComponent } from '../../layouts/responsable-layout/responsable-layout.component';
import { Role } from '../../core/enums/role.enum';

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
				data: { scope: 'responsable', baseRoute: '/responsable' },
				loadComponent: () => import('../shared/self-demande/mes-demandes.component').then(m => m.SelfMesDemandesComponent),
			},
			{
				path: 'nouvelle-demande',
				data: { scope: 'responsable', baseRoute: '/responsable' },
				loadComponent: () => import('../shared/self-demande/nouvelle-demande.component').then(m => m.SelfNouvelleDemandeComponent),
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
				data: { role: Role.RESPONSABLE },
				loadComponent: () => import('../shared/profil/profil.component').then(m => m.SharedProfilComponent),
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
