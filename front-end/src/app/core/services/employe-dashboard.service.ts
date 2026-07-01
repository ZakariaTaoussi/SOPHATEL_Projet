import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { EmployeDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class EmployeDashboardService {
  private readonly baseUrl = apiUrl('/api/employe/dashboard');

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<EmployeDashboardResponse> {
    return this.http.get<EmployeDashboardResponse>(this.baseUrl, { withCredentials: true });
  }
}
