import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { ResponsableDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class ResponsableDashboardService {
  private readonly baseUrl = apiUrl('/api/responsable/dashboard');

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<ResponsableDashboardResponse> {
    return this.http.get<ResponsableDashboardResponse>(this.baseUrl, { withCredentials: true });
  }
}
