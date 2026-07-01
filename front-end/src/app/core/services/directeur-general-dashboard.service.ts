import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { DirecteurGeneralDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DirecteurGeneralDashboardService {
  private readonly baseUrl = apiUrl('/api/directeur-general/dashboard');

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<DirecteurGeneralDashboardResponse> {
    return this.http.get<DirecteurGeneralDashboardResponse>(this.baseUrl, { withCredentials: true });
  }
}
