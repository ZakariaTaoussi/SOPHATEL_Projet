import { Routes } from '@angular/router';
import { Role } from './core/enums/role.enum';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },


  {
    path: 'employe',
    canActivate: [roleGuard],
    data: { roles: [Role.EMPLOYE] },
    loadChildren: () =>
      import('./features/employe/employe.routes').then(m => m.EMPLOYE_ROUTES),
   
  },
  {
    path: 'responsable',
    canActivate: [roleGuard],
    data: { roles: [Role.RESPONSABLE] },
    loadChildren: () =>
      import('./features/responsable/responsable.routes').then(m => m.RESPONSABLE_ROUTES),
  },
  {
    path: 'rh',
    canActivate: [roleGuard],
    data: { roles: [Role.RH] },
    loadChildren: () =>
      import('./features/rh/rh.routes').then(m => m.RH_ROUTES),
  },
  {
    path: 'directeur-general',
    canActivate: [roleGuard],
    data: { roles: [Role.DIRECTEUR_GENERAL] },
    loadChildren: () =>
      import('./features/directeur-general/dg.route').then(m => m.DIRECTEUR_GENERAL_ROUTES),
  },
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { roles: [Role.ADMINISTRATEUR] },
    loadChildren: () =>
      import('./features/admin/admin.route').then(m => m.ADMIN_ROUTES),
  },


 { path: '**', redirectTo: 'auth/login' },
];
