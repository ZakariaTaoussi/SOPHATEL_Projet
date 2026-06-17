import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { ResponsableDemande } from '../models/demande-conge.model';

@Injectable({ providedIn: 'root' })
export class DirecteurGeneralDemandeService {
  private readonly directeurGeneralUrl = apiUrl('/api/directeur-general');

  constructor(private readonly http: HttpClient) {}

  getDemandesAValider(): Observable<ResponsableDemande[]> {
    return this.http.get<ResponsableDemande[]>(`${this.directeurGeneralUrl}/demandes-a-valider`, {
      withCredentials: true,
    });
  }
}
