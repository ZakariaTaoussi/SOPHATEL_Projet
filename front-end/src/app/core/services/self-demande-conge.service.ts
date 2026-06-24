import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, throwError } from 'rxjs';
import { apiUrl } from '../config/api-url';
import {
  DemandeConge,
  DemandeCongeCreateRequest,
  DemandeCongeUpdateRequest,
  AbsenceStatsResponse,
  SoldeConge,
} from '../models/demande-conge.model';

export type SelfDemandeScope = 'employe' | 'rh' | 'responsable' | 'directeur-general';
type DemandeListResponse = DemandeConge[] | { content: DemandeConge[] };

@Injectable({ providedIn: 'root' })
export class SelfDemandeCongeService {
  constructor(private readonly http: HttpClient) {}

  getMesDemandes(scope: SelfDemandeScope): Observable<DemandeConge[]> {
    return this.http.get<DemandeListResponse>(this.demandesUrl(scope), { withCredentials: true })
      .pipe(map(response => this.toDemandesArray(response)));
  }

  getMesAbsences(scope: SelfDemandeScope): Observable<DemandeConge[]> {
    return this.http.get<DemandeListResponse>(this.absencesUrl(scope), { withCredentials: true })
      .pipe(map(response => this.toDemandesArray(response)));
  }

  getAbsenceStats(scope: SelfDemandeScope, year: number): Observable<AbsenceStatsResponse[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<AbsenceStatsResponse[]>(`${this.absencesUrl(scope)}/stats`, {
      params,
      withCredentials: true,
    });
  }

  getDemandeById(scope: SelfDemandeScope, id: number): Observable<DemandeConge> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('Id de demande manquant'));
    }

    return this.http.get<DemandeConge>(`${this.demandesUrl(scope)}/${id}`, { withCredentials: true });
  }

  creerDemande(scope: SelfDemandeScope, payload: DemandeCongeCreateRequest): Observable<DemandeConge> {
    return this.http.post<DemandeConge>(this.demandesUrl(scope), payload, { withCredentials: true });
  }

  modifierDemande(
    scope: SelfDemandeScope,
    id: number,
    payload: DemandeCongeUpdateRequest
  ): Observable<DemandeConge> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('Id de demande manquant'));
    }

    return this.http.put<DemandeConge>(`${this.demandesUrl(scope)}/${id}`, payload, { withCredentials: true });
  }

  submitDemande(scope: SelfDemandeScope, id: number): Observable<DemandeConge> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('Id de demande manquant'));
    }

    return this.http.post<DemandeConge>(`${this.demandesUrl(scope)}/${id}/submit`, {}, { withCredentials: true });
  }

  passerEnModification(scope: SelfDemandeScope, id: number): Observable<DemandeConge> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('Id de demande manquant'));
    }

    return this.http.post<DemandeConge>(`${this.demandesUrl(scope)}/${id}/modifier`, {}, { withCredentials: true });
  }

  annulerDemande(scope: SelfDemandeScope, id: number): Observable<DemandeConge> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('Id de demande manquant'));
    }

    return this.http.post<DemandeConge>(`${this.demandesUrl(scope)}/${id}/annuler`, {}, { withCredentials: true });
  }

  getSoldeConge(scope: SelfDemandeScope): Observable<SoldeConge> {
    return this.http.get<SoldeConge>(this.soldeUrl(scope), { withCredentials: true });
  }

  calculerJours(scope: SelfDemandeScope, dateDebut: string, dateFin: string): Observable<{ jours: number }> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    return this.http.get<{ jours: number }>(`${this.demandesUrl(scope)}/calcul-jours`, {
      params,
      withCredentials: true,
    });
  }

  private demandesUrl(scope: SelfDemandeScope): string {
    switch (scope) {
      case 'rh':
        return apiUrl('/api/rh/demandes');
      case 'responsable':
        return apiUrl('/api/responsable/mes-demandes');
      case 'directeur-general':
        return apiUrl('/api/directeur-general/mes-demandes');
      default:
        return apiUrl('/api/employe/demandes');
    }
  }

  private absencesUrl(scope: SelfDemandeScope): string {
    switch (scope) {
      case 'rh':
        return apiUrl('/api/rh/absences');
      case 'responsable':
        return apiUrl('/api/responsable/absences');
      default:
        return apiUrl('/api/employe/absences');
    }
  }

  private soldeUrl(scope: SelfDemandeScope): string {
    switch (scope) {
      case 'rh':
        return apiUrl('/api/rh/solde-conge');
      case 'responsable':
        return apiUrl('/api/responsable/solde-conge');
      case 'directeur-general':
        return apiUrl('/api/directeur-general/solde-conge');
      default:
        return apiUrl('/api/employe/solde-conge');
    }
  }

  private isValidId(id: number): boolean {
    return Number.isFinite(id) && id > 0;
  }

  private toDemandesArray(response: DemandeListResponse): DemandeConge[] {
    return Array.isArray(response) ? response : response.content ?? [];
  }
}
