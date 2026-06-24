import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { DemandeCongeImpression } from '../models/demande-conge.model';

@Injectable({ providedIn: 'root' })
export class DemandeCongeImpressionService {
  constructor(private readonly http: HttpClient) {}

  getDemandePourImpression(id: number): Observable<DemandeCongeImpression> {
    return this.http.get<DemandeCongeImpression>(apiUrl(`/api/demandes/${id}/impression`), {
      withCredentials: true,
    });
  }
}
