import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthSessionService } from './auth-session.service';

export const superAdminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const session = inject(AuthSessionService);
  return session.role === 'SuperAdmin' ? true : router.createUrlTree(['/']);
};
