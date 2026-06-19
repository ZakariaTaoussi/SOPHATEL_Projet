import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { ResponsableEmploye } from '../models/responsable-employe.model';

@Injectable({ providedIn: 'root' })
export class ResponsableEmployeService {
  private readonly baseUrl = apiUrl('/api/responsable/mes-employes');

  constructor(private readonly http: HttpClient) {}

  getMesEmployes(): Observable<ResponsableEmploye[]> {
    return this.http.get<ResponsableEmploye[]>(this.baseUrl, { withCredentials: true });
  }

  getEmployeById(id: number): Observable<ResponsableEmploye> {
    return this.http.get<ResponsableEmploye>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }
}
