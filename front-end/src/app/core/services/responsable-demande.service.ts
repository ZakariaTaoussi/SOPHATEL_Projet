import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import {
  ResponsableDemande,
  ResponsableValidationDemandeRequest,
} from '../models/demande-conge.model';

@Injectable({ providedIn: 'root' })
export class ResponsableDemandeService {
  private readonly responsableUrl = apiUrl('/api/responsable');

  constructor(private readonly http: HttpClient) {}

  getDemandesAValider(): Observable<ResponsableDemande[]> {
    return this.http.get<ResponsableDemande[]>(`${this.responsableUrl}/demandes-a-valider`, {
      withCredentials: true,
    });
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
    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/demandes/${id}/refuser`, {}, {
      withCredentials: true,
    });
  }

  passerEnModificationResponsable(id: number): Observable<ResponsableDemande> {
    return this.http.post<ResponsableDemande>(`${this.responsableUrl}/demandes/${id}/modifier`, {}, {
      withCredentials: true,
    });
  }
}
