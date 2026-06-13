import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { Role } from '../enums/role.enum';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const expectedRoles = (route.data?.['roles'] as Role[] | undefined) ?? [];

  return authService.ensureAuthState().pipe(
    map(user => {
      if (!user) {
        return router.parseUrl('/auth/login');
      }

      if (expectedRoles.length === 0 || expectedRoles.includes(user.role)) {
        return true;
      }

      return router.parseUrl(authService.getDashboardByRole(user.role));
    })
  );
};
