import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { CreateJourFerieRequest, JourFerie, UpdateJourFerieRequest } from '../models/jour-ferie.model';

@Injectable({ providedIn: 'root' })
export class JourFerieService {
  private readonly baseUrl = apiUrl('/api/admin/jours-feries');

  constructor(private readonly http: HttpClient) {}

  creerJourFerie(request: CreateJourFerieRequest): Observable<JourFerie> {
    return this.http.post<JourFerie>(this.baseUrl, request, { withCredentials: true });
  }

  modifierJourFerie(id: number, request: UpdateJourFerieRequest): Observable<JourFerie> {
    return this.http.put<JourFerie>(`${this.baseUrl}/${id}`, request, { withCredentials: true });
  }

  supprimerJourFerie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }

  consulterJoursFeriesParAnnee(annee: number): Observable<JourFerie[]> {
    return this.http.get<JourFerie[]>(`${this.baseUrl}?annee=${annee}`, { withCredentials: true });
  }

  consulterJourFerie(id: number): Observable<JourFerie> {
    return this.http.get<JourFerie>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }
}
