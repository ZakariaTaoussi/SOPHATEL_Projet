import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, throwError } from 'rxjs';
import { apiUrl } from '../config/api-url';
import {
  ResponsableDemande,
  ResponsableValidationDemandeRequest,
} from '../models/demande-conge.model';

type ResponsableDemandeListResponse = ResponsableDemande[] | { content: ResponsableDemande[] };

@Injectable({ providedIn: 'root' })
export class ResponsableDemandeService {
  private readonly responsableUrl = apiUrl('/api/responsable');

  constructor(private readonly http: HttpClient) {}

  getDemandesAValider(): Observable<ResponsableDemande[]> {
    return this.http.get<ResponsableDemandeListResponse>(`${this.responsableUrl}/demandes-a-valider`, {
      withCredentials: true,
    }).pipe(map(response => this.toDemandesArray(response)));
  }

  getDemandesValidees(): Observable<ResponsableDemande[]> {
    return this.http.get<ResponsableDemandeListResponse>(`${this.responsableUrl}/demandes-validees`, {
      withCredentials: true,
    }).pipe(map(response => this.toDemandesArray(response)));
  }

  getAbsencesAValider(): Observable<ResponsableDemande[]> {
    return this.http.get<ResponsableDemandeListResponse>(`${this.responsableUrl}/absences-a-valider`, {
      withCredentials: true,
    }).pipe(map(response => this.toDemandesArray(response)));
  }

  getDemandeById(id: number): Observable<ResponsableDemande> {
    return this.http.get<ResponsableDemande>(`${this.responsableUrl}/demandes-a-valider/${id}`, {
      withCredentials: true,
    });
  }

  validerDemande(
    id: number,
    payload: ResponsableValidationDemandeRequest
  ): Observable<ResponsableDemande> {
    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/demandes/${id}/valider`, payload, {
      withCredentials: true,
    });
  }

  refuserDemande(id: number): Observable<ResponsableDemande> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('id demande manquant'));
    }

    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/demandes/${id}/refuser`, {}, {
      withCredentials: true,
    });
  }

  validerAbsence(
    id: number,
    payload: ResponsableValidationDemandeRequest
  ): Observable<ResponsableDemande> {
    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/absences/${id}/valider`, payload, {
      withCredentials: true,
    });
  }

  refuserAbsence(id: number): Observable<ResponsableDemande> {
    if (!this.isValidId(id)) {
      return throwError(() => new Error('id demande manquant'));
    }

    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/absences/${id}/refuser`, {}, {
      withCredentials: true,
    });
  }

  passerEnModificationResponsable(id: number): Observable<ResponsableDemande> {
    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/demandes/${id}/modifier`, {}, {
      withCredentials: true,
    });
  }

  private isValidId(id: number): boolean {
    return Number.isFinite(id) && id > 0;
  }

  private toDemandesArray(response: ResponsableDemandeListResponse): ResponsableDemande[] {
    return Array.isArray(response) ? response : response.content ?? [];
  }
}
