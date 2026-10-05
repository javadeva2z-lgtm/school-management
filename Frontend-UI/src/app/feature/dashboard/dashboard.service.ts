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
          item.id === 'classes-sections' || item.id === 'subjects' || item.id === 'optional-fee-mapping'
        );
        const missingRequiredMenus = requiredAdminMenus.filter(menu =>
          !adminMenus.some(item => item.id === menu.id)
        );
        return {
          ...data,
          menus: {
            ...data.menus,
            Admin: [...adminMenus, ...missingRequiredMenus]
          }
        };
      }),
      catchError(() => of(FALLBACK_DASHBOARD_DATA))
    );
  }
}
