import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { PageResponse } from '../models/page-response.model';
import { RhDemandeSuivi, RhDepartement } from '../models/rh-demande-suivi.model';

@Injectable({ providedIn: 'root' })
export class RhSuiviDemandesService {
  private readonly baseUrl = apiUrl('/api/rh/suivi');

  constructor(private readonly http: HttpClient) {}

  getCongesValides(
    page = 0,
    size = 4,
    annee?: number | null,
    mois?: number | null,
    search?: string | null,
    departementId?: number | null
  ): Observable<PageResponse<RhDemandeSuivi>> {
    return this.http.get<PageResponse<RhDemandeSuivi>>(`${this.baseUrl}/conges`, {
      params: this.buildParams(page, size, annee, mois, search, departementId),
      withCredentials: true,
    });
  }

  getAbsencesValidees(
    page = 0,
    size = 4,
    annee?: number | null,
    mois?: number | null,
    search?: string | null,
    departementId?: number | null
  ): Observable<PageResponse<RhDemandeSuivi>> {
    return this.http.get<PageResponse<RhDemandeSuivi>>(`${this.baseUrl}/absences`, {
      params: this.buildParams(page, size, annee, mois, search, departementId),
      withCredentials: true,
    });
  }

  exportCongesExcel(
    annee?: number | null,
    mois?: number | null,
    search?: string | null,
    departementId?: number | null
  ): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/conges/export`, {
      params: this.buildParams(null, null, annee, mois, search, departementId),
      responseType: 'blob',
      withCredentials: true,
    });
  }

  exportAbsencesExcel(
    annee?: number | null,
    mois?: number | null,
    search?: string | null,
    departementId?: number | null
  ): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/absences/export`, {
      params: this.buildParams(null, null, annee, mois, search, departementId),
      responseType: 'blob',
      withCredentials: true,
    });
  }

  getDepartements(): Observable<RhDepartement[]> {
    return this.http.get<RhDepartement[]>(`${this.baseUrl}/departements`, { withCredentials: true });
  }

  private buildParams(
    page?: number | null,
    size?: number | null,
    annee?: number | null,
    mois?: number | null,
    search?: string | null,
    departementId?: number | null
  ): HttpParams {
    let params = new HttpParams();
    if (page !== null && page !== undefined) {
      params = params.set('page', page);
    }
    if (size !== null && size !== undefined) {
      params = params.set('size', size);
    }
    if (annee) {
      params = params.set('annee', annee);
    }
    if (mois) {
      params = params.set('mois', mois);
    }
    if (search?.trim()) {
      params = params.set('search', search.trim());
    }
    if (departementId) {
      params = params.set('departementId', departementId);
    }
    return params;
  }
}
