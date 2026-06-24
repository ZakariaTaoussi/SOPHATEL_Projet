import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, tap } from 'rxjs';
import { apiUrl } from '../config/api-url';
import {
  DemandeConge,
  DemandeCongeCreateRequest,
  DemandeCongeUpdateRequest,
  AbsenceStatsResponse,
  SoldeConge,
} from '../models/demande-conge.model';

type DemandeListResponse = DemandeConge[] | { content: DemandeConge[] };

@Injectable({ providedIn: 'root' })
export class DemandeCongeService {
  private readonly employeUrl = apiUrl('/api/employe');
  private readonly responsableUrl = apiUrl('/api/responsable');
  private readonly mesDemandesSubject = new BehaviorSubject<DemandeConge[]>([]);
  private readonly soldeCongeSubject = new BehaviorSubject<SoldeConge | null>(null);
  private readonly demandesAValiderSubject = new BehaviorSubject<DemandeConge[]>([]);

  readonly mesDemandes$ = this.mesDemandesSubject.asObservable();
  readonly soldeConge$ = this.soldeCongeSubject.asObservable();
  readonly demandesAValider$ = this.demandesAValiderSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  getMesDemandes(): Observable<DemandeConge[]> {
    return this.http.get<DemandeListResponse>(`${this.employeUrl}/demandes`, { withCredentials: true })
      .pipe(map(response => this.toDemandesArray(response)))
      .pipe(tap(demandes => this.mesDemandesSubject.next(demandes)));
  }

  getMesAbsences(): Observable<DemandeConge[]> {
    return this.http.get<DemandeListResponse>(`${this.employeUrl}/absences`, { withCredentials: true })
      .pipe(map(response => this.toDemandesArray(response)));
  }

  getAbsenceStats(year: number): Observable<AbsenceStatsResponse[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<AbsenceStatsResponse[]>(`${this.employeUrl}/absences/stats`, {
      params,
      withCredentials: true,
    });
  }

  getDemandeById(id: number): Observable<DemandeConge> {
    return this.http.get<DemandeConge>(`${this.employeUrl}/demandes/${id}`, { withCredentials: true });
  }

  creerDemande(payload: DemandeCongeCreateRequest): Observable<DemandeConge> {
    return this.http.post<DemandeConge>(`${this.employeUrl}/demandes`, payload, { withCredentials: true })
      .pipe(tap(demande => this.upsertMesDemande(demande)));
  }

  modifierDemande(id: number, payload: DemandeCongeUpdateRequest): Observable<DemandeConge> {
    return this.http.put<DemandeConge>(`${this.employeUrl}/demandes/${id}`, payload, { withCredentials: true })
      .pipe(tap(demande => this.upsertMesDemande(demande)));
  }

  submitDemande(id: number): Observable<DemandeConge> {
    return this.http.post<DemandeConge>(`${this.employeUrl}/demandes/${id}/submit`, {}, { withCredentials: true })
      .pipe(tap(demande => this.afterDemandeMutation(demande)));
  }

  passerEnModification(id: number): Observable<DemandeConge> {
    return this.http.post<DemandeConge>(`${this.employeUrl}/demandes/${id}/modifier`, {}, { withCredentials: true })
      .pipe(tap(demande => this.afterDemandeMutation(demande)));
  }

  annulerDemande(id: number): Observable<DemandeConge> {
    return this.http.post<DemandeConge>(`${this.employeUrl}/demandes/${id}/annuler`, {}, { withCredentials: true })
      .pipe(tap(demande => this.afterDemandeMutation(demande)));
  }

  getSoldeConge(): Observable<SoldeConge> {
    return this.http.get<SoldeConge>(`${this.employeUrl}/solde-conge`, { withCredentials: true })
      .pipe(tap(solde => this.soldeCongeSubject.next(solde)));
  }

  calculerJours(dateDebut: string, dateFin: string): Observable<{ jours: number }> {
    const params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    return this.http.get<{ jours: number }>(`${this.employeUrl}/demandes/calcul-jours`, {
      params,
      withCredentials: true,
    });
  }

  getDemandesAValiderResponsable(): Observable<DemandeConge[]> {
    return this.http.get<DemandeConge[]>(`${this.responsableUrl}/demandes-a-valider`, { withCredentials: true })
      .pipe(tap(demandes => this.demandesAValiderSubject.next(demandes)));
  }

  refreshMesDemandes(): void {
    this.getMesDemandes().subscribe({ error: error => console.error('Erreur refresh demandes', error) });
  }

  refreshSoldeConge(): void {
    this.getSoldeConge().subscribe({ error: error => console.error('Erreur refresh solde conge', error) });
  }

  refreshDemandesAValiderResponsable(): void {
    this.getDemandesAValiderResponsable().subscribe({
      error: error => console.error('Erreur refresh demandes responsable', error),
    });
  }

  private afterDemandeMutation(demande: DemandeConge): void {
    this.upsertMesDemande(demande);
    this.refreshSoldeConge();
    this.refreshMesDemandes();
  }

  private upsertMesDemande(demande: DemandeConge): void {
    const demandes = this.mesDemandesSubject.value;
    const exists = demandes.some(item => item.id === demande.id);
    const updated = exists
      ? demandes.map(item => item.id === demande.id ? demande : item)
      : [demande, ...demandes];

    this.mesDemandesSubject.next(updated);
  }

  private toDemandesArray(response: DemandeListResponse): DemandeConge[] {
    return Array.isArray(response) ? response : response.content ?? [];
  }
}
