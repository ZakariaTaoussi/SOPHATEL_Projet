import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { Agenda, CreateAgendaRequest } from '../models/agenda.model';
import { JourCalendrier } from '../models/jour-calendrier.model';

@Injectable({ providedIn: 'root' })
export class AgendaService {
  private readonly baseUrl = apiUrl('/api/admin/agendas');

  constructor(private readonly http: HttpClient) {}

  creerAgenda(request: CreateAgendaRequest): Observable<Agenda> {
    return this.http.post<Agenda>(this.baseUrl, request, { withCredentials: true });
  }

  consulterAgendas(): Observable<Agenda[]> {
    return this.http.get<Agenda[]>(this.baseUrl, { withCredentials: true });
  }

  consulterAgendaParAnnee(annee: number): Observable<Agenda> {
    return this.http.get<Agenda>(`${this.baseUrl}/annee/${annee}`, { withCredentials: true });
  }

  consulterJoursCalendrier(annee: number): Observable<JourCalendrier[]> {
    return this.http.get<JourCalendrier[]>(`${this.baseUrl}/${annee}/jours`, { withCredentials: true });
  }

  supprimerAgenda(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }
}
