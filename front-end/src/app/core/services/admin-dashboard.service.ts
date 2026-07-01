import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { AdminDashboardResponse } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private readonly baseUrl = apiUrl('/api/admin/dashboard');

  constructor(private readonly http: HttpClient) {}

  getDashboard(): Observable<AdminDashboardResponse> {
    return this.http.get<AdminDashboardResponse>(this.baseUrl, { withCredentials: true });
  }
}
