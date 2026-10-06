import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, tap } from 'rxjs';

import { AuthSessionService } from './auth-session.service';
import { LOGIN_URL, usersApiUrl } from '../config/api.config';
import { LoginRequest, LoginResponse, PasswordManagedUser, Role, UserRegistrationRequest, UserRegistrationResponse } from '../../common/model/models';


@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(AuthSessionService);

  login(credentials: LoginRequest): Observable<void> {
    return this.http.post<LoginResponse>(LOGIN_URL, credentials).pipe(
      map(response => {
        const token = response.data.token;
        const mappedRoles = response.data.roles
          .map(value => this.toRole(value))
          .filter((value): value is Role => value !== null);
        const role = mappedRoles.includes('SuperAdmin') ? 'SuperAdmin' : mappedRoles[0] ?? null;
        if (!token || !role) {
          throw new Error('Login response did not include a valid token and role.');
        }
        return { token, role };
      }),
      tap(({ token, role }) => this.session.setSession(token, role)),
      map(() => undefined)
    );
  }

  createAdmin(adminReq: UserRegistrationRequest, token: string): Observable<void> {
    return this.http.post<UserRegistrationResponse>(usersApiUrl('/register/admin/' + token), adminReq).pipe(
      map(response => {
        if (response.code > 299) {
          throw new Error('Admin creation failed.');
        }
      })
    );
  }

  createAdminOrManager(request: UserRegistrationRequest): Observable<void> {
    return this.http.post<UserRegistrationResponse>(usersApiUrl('/register/admin-manager'), request).pipe(
      map(response => {
        if (response.code > 299) {
          throw new Error('User creation failed.');
        }
      })
    );
  }

  getManagers(): Observable<{ username: string; phoneNumber: string | null }[]> {
    return this.http.get<{ data: { username: string; phoneNumber: string | null }[] }>(
      usersApiUrl('/managers')
    ).pipe(
      map(response => response.data ?? [])
    );
  }

  deleteManager(username: string): Observable<void> {
    return this.http.delete<{ code: number }>(
      usersApiUrl(`/managers/${encodeURIComponent(username)}`)
    ).pipe(
      map(response => {
        if (response.code > 299) {
          throw new Error('Manager deletion failed.');
        }
      })
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<{ code: number }>(usersApiUrl('/password/change'), {
      currentPassword,
      newPassword
    }).pipe(
      map(response => {
        if (response.code > 299) {
          throw new Error('Password change failed.');
        }
      })
    );
  }

  getPasswordManagedUsers(): Observable<PasswordManagedUser[]> {
    return this.http.get<{ data: PasswordManagedUser[] }>(
      usersApiUrl('/password/managed-users')
    ).pipe(
      map(response => response.data ?? [])
    );
  }

  adminResetPassword(username: string, newPassword: string): Observable<void> {
    return this.http.put<{ code: number }>(
      usersApiUrl(`/password/admin-reset/${encodeURIComponent(username)}`),
      { newPassword }
    ).pipe(
      map(response => {
        if (response.code > 299) {
          throw new Error('Password reset failed.');
        }
      })
    );
  }

  getAdminToken(): Observable<string> {
    return this.http.get<{ data: string }>(usersApiUrl('/register/admin/token')).pipe(
      map(response => response.data),
      catchError(() => of(''))
    );
  }

  setSchoolCode(schoolCode: string): void {
    this.session.setSchoolCode(schoolCode);
  }

  get schoolHeaderValue(): string | null {
    return this.session.schoolHeaderValue;
  }

  logout(): void {
    this.session.clearSession();
  }

  private toRole(value: string | undefined): Role | null {
    switch (value?.toLowerCase().replace(/^role_/, '')) {
      case 'teacher': return 'Teacher';
      case 'student': return 'Student';
      case 'admin': return 'Admin';
      case 'manager': return 'Manager';
      case 'super_admin': return 'SuperAdmin';
      default: return null;
    }
  }
}
