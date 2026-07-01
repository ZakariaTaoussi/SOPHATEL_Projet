import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { RhDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class RhDashboardService {
  private readonly baseUrl = apiUrl('/api/rh/dashboard');

  constructor(private readonly http: HttpClient) {}

  getDashboard(annee?: number | null, mois?: number | null): Observable<RhDashboardResponse> {
    let params = new HttpParams();
    if (annee) {
      params = params.set('annee', annee);
    }
    if (mois) {
      params = params.set('mois', mois);
    }
    return this.http.get<RhDashboardResponse>(this.baseUrl, { params, withCredentials: true });
  }
}
