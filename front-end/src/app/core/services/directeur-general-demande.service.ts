import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, throwError } from 'rxjs';
import { apiUrl } from '../config/api-url';
import {
  DirecteurGeneralDemande,
  DirecteurGeneralValidationDemandeRequest,
} from '../models/demande-conge.model';

type DirecteurGeneralDemandeListResponse = DirecteurGeneralDemande[] | { content: DirecteurGeneralDemande[] };

@Injectable({ providedIn: 'root' })
export class DirecteurGeneralDemandeService {
  private readonly directeurGeneralUrl = apiUrl('/api/directeur-general');

  constructor(private readonly http: HttpClient) {}

  getDemandesAValider(): Observable<DirecteurGeneralDemande[]> {
    return this.http.get<DirecteurGeneralDemandeListResponse>(`${this.directeurGeneralUrl}/demandes-a-valider`, {
      withCredentials: true,
    }).pipe(map(response => this.toDemandesArray(response)));
  }

  getAbsencesAValider(): Observable<DirecteurGeneralDemande[]> {
    return this.http.get<DirecteurGeneralDemandeListResponse>(`${this.directeurGeneralUrl}/absences-a-valider`, {
      withCredentials: true,
    }).pipe(map(response => this.toDemandesArray(response)));
  }

  getDemandeById(id: number): Observable<DirecteurGeneralDemande> {
    return this.http.get<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/demandes-a-valider/${id}`, {
      withCredentials: true,
    });
  }

  validerDemande(
    id: number,
    payload: DirecteurGeneralValidationDemandeRequest
  ): Observable<DirecteurGeneralDemande> {
    return this.http.post<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/demandes/${id}/valider`, payload, {
      withCredentials: true,
    });
  }

  refuserDemande(id: number): Observable<DirecteurGeneralDemande> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('id demande manquant'));
    }

    return this.http.post<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/demandes/${id}/refuser`, {}, {
      withCredentials: true,
    });
  }

  validerAbsence(
    id: number,
    payload: DirecteurGeneralValidationDemandeRequest
  ): Observable<DirecteurGeneralDemande> {
    return this.http.post<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/absences/${id}/valider`, payload, {
      withCredentials: true,
    });
  }

  refuserAbsence(id: number): Observable<DirecteurGeneralDemande> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('id demande manquant'));
    }

    return this.http.post<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/absences/${id}/refuser`, {}, {
      withCredentials: true,
    });
  }

  passerEnModificationDg(id: number): Observable<DirecteurGeneralDemande> {
    return this.http.post<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/demandes/${id}/modifier`, {}, {
      withCredentials: true,
    });
  }

  getDemandesValidees(): Observable<DirecteurGeneralDemande[]> {
    return this.http.get<DirecteurGeneralDemandeListResponse>(`${this.directeurGeneralUrl}/demandes-validees`, {
      withCredentials: true,
    }).pipe(map(response => this.toDemandesArray(response)));
  }

  private isValidId(id: number): boolean {
    return Number.isFinite(id) && id > 0;
  }

  private toDemandesArray(response: DirecteurGeneralDemandeListResponse): DirecteurGeneralDemande[] {
    return Array.isArray(response) ? response : response.content ?? [];
  }
}
