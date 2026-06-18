import { Routes } from '@angular/router';
import { RhLayoutComponent } from '../../layouts/rh-layout/rh-layout.component';

export const RH_ROUTES: Routes = [
	{
		path: '',
		component: RhLayoutComponent,
		children: [
			{ path: 'dashboard', loadComponent: () => import('./dashboard/dashboard.component').then(m => m.RhDashboardComponent) },
			{ path: 'Employes', loadComponent: () => import('./employes/employes.component').then(m => m.RhEmployesComponent) },
			{
				path: 'mes-demandes',
				data: { scope: 'rh', baseRoute: '/rh' },
				loadComponent: () => import('../shared/self-demande/mes-demandes.component').then(m => m.SelfMesDemandesComponent),
			},
			{ path: 'mes-absences', loadComponent: () => import('./mes-absences/mes-absences.component').then(m => m.RhMesAbsencesComponent) },
			{ path: 'declaration-absences', loadComponent: () => import('./declaration-absences/declaration-absences.component').then(m => m.RhDeclarationAbsencesComponent) },
			{
				path: 'nouvelle-demande',
				data: { scope: 'rh', baseRoute: '/rh' },
				loadComponent: () => import('../shared/self-demande/nouvelle-demande.component').then(m => m.SelfNouvelleDemandeComponent),
			},
			{ path: 'notification', loadComponent: () => import('./notification/notification.component').then(m => m.RhNotificationComponent) },
			{ path: 'profil', loadComponent: () => import('./profil/profil.component').then(m => m.RhProfilComponent) },
			{ path: '', redirectTo: 'dashboard', pathMatch: 'full' },
		],
	},
];
