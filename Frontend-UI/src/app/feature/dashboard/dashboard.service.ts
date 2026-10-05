import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';

import { FALLBACK_DASHBOARD_DATA } from '../../common/model/dashboard.models';
import { DashboardData } from '../../common/model/models';
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  getDashboardData(): Observable<DashboardData> {
    return this.http.get<DashboardData>('/api/dashboard').pipe(
      map(data => {
        const adminMenus = data.menus.Admin ?? [];
        const requiredAdminMenus = FALLBACK_DASHBOARD_DATA.menus.Admin.filter(item =>
          ['classes-sections', 'subjects', 'optional-fee-mapping', 'user-accounts'].includes(item.id)
        );
        const missingRequiredMenus = requiredAdminMenus.filter(menu =>
          !adminMenus.some(item => item.id === menu.id)
        );
        const adminMenuItems = [...adminMenus, ...missingRequiredMenus];
        const managerMenus = adminMenuItems
          .filter(item => item.id !== 'import-data' && item.id !== 'user-accounts')
          .map(item => ({ ...item, roles: ['Manager' as const] }));
        const superAdminMenus = data.menus.SuperAdmin?.length
          ? data.menus.SuperAdmin
          : FALLBACK_DASHBOARD_DATA.menus.SuperAdmin;
        return {
          ...data,
          menus: {
            ...data.menus,
            Admin: adminMenuItems,
            Manager: managerMenus,
            SuperAdmin: superAdminMenus
          }
        };
      }),
      catchError(() => of(FALLBACK_DASHBOARD_DATA))
    );
  }
}
