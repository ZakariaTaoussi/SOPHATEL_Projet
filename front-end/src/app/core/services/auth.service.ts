import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { Role } from '../enums/role.enum';
import { AuthUser } from '../models/auth-user.model';

export interface LoginCredentials {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = apiUrl('/api/auth');
  private readonly currentUserSignal = signal<AuthUser | null>(null);
  private initialized = false;

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly role = computed(() => this.currentUserSignal()?.role ?? null);

  constructor(private http: HttpClient) {}

  login(credentials: LoginCredentials): Observable<AuthUser> {
    return this.http
      .post<AuthUser>(`${this.apiUrl}/login`, credentials, { withCredentials: true })
      .pipe(tap(user => {
        this.initialized = true;
        this.currentUserSignal.set(user);
      }));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true })
      .pipe(
        catchError(() => of(void 0)),
        tap(() => {
          this.initialized = true;
          this.currentUserSignal.set(null);
        })
      );
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSignal();
  }

  getCurrentUserFromApi(): Observable<AuthUser> {
    return this.http
      .get<AuthUser>(`${this.apiUrl}/me`, { withCredentials: true })
      .pipe(tap(user => {
        this.initialized = true;
        this.currentUserSignal.set(user);
      }));
  }

  initializeAuthState(): Observable<AuthUser | null> {
    return this.getCurrentUserFromApi().pipe(
      map(user => user),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          this.currentUserSignal.set(null);
          this.initialized = true;
          return of(null);
        }

        this.currentUserSignal.set(null);
        this.initialized = true;
        return of(null);
      })
    );
  }

  ensureAuthState(): Observable<AuthUser | null> {
    if (this.initialized) {
      return of(this.currentUserSignal());
    }

    return this.initializeAuthState();
  }

  isAuthenticated(): boolean {
    return this.currentUserSignal() !== null;
  }

  getDashboardByRole(role: Role): string {
    switch (role) {
      case Role.EMPLOYE:
        return '/employe/dashboard';
      case Role.RH:
        return '/rh/dashboard';
      case Role.RESPONSABLE:
        return '/responsable/dashboard';
      case Role.ADMINISTRATEUR:
        return '/admin/dashboard';
      case Role.DIRECTEUR_GENERAL:
        return '/directeur-general/dashboard';
    }
  }
}
