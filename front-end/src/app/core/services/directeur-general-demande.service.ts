import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { apiUrl } from '../config/api-url';
import {
  DirecteurGeneralDemande,
  DirecteurGeneralValidationDemandeRequest,
} from '../models/demande-conge.model';

@Injectable({ providedIn: 'root' })
export class DirecteurGeneralDemandeService {
  private readonly directeurGeneralUrl = apiUrl('/api/directeur-general');

  constructor(private readonly http: HttpClient) {}

  getDemandesAValider(): Observable<DirecteurGeneralDemande[]> {
    return this.http.get<DirecteurGeneralDemande[]>(`${this.directeurGeneralUrl}/demandes-a-valider`, {
      withCredentials: true,
    });
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

  passerEnModificationDg(id: number): Observable<DirecteurGeneralDemande> {
    return this.http.post<DirecteurGeneralDemande>(`${this.directeurGeneralUrl}/demandes/${id}/modifier`, {}, {
      withCredentials: true,
    });
  }

  getDemandesValidees(): Observable<DirecteurGeneralDemande[]> {
    return this.http.get<DirecteurGeneralDemande[]>(`${this.directeurGeneralUrl}/demandes-validees`, {
      withCredentials: true,
    });
  }

  private isValidId(id: number): boolean {
    return Number.isFinite(id) && id > 0;
  }
}
