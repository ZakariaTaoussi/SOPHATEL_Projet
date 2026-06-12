import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },


  {
    path: 'employe',
    loadChildren: () =>
      import('./features/employe/employe.routes').then(m => m.EMPLOYE_ROUTES),
   
  },
  {
    path: 'responsable',
    loadChildren: () =>
      import('./features/responsable/responsable.routes').then(m => m.RESPONSABLE_ROUTES),
  },
  {
    path: 'rh',
    loadChildren: () =>
      import('./features/rh/rh.routes').then(m => m.RH_ROUTES),
  },
  {
    path: 'directeur-general',
    loadChildren: () =>
      import('./features/directeur-general/dg.route').then(m => m.DIRECTEUR_GENERAL_ROUTES),
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./features/admin/admin.route').then(m => m.ADMIN_ROUTES),
  },


 { path: '**', redirectTo: 'employe' },
];
