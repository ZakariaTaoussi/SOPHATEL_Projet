import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { PageResponse } from '../models/page-response.model';
import { ResponsableEmploye } from '../models/responsable-employe.model';

@Injectable({ providedIn: 'root' })
export class ResponsableEmployeService {
  private readonly baseUrl = apiUrl('/api/responsable/mes-employes');

  constructor(private readonly http: HttpClient) {}

  getMesEmployes(page = 0, size = 4): Observable<PageResponse<ResponsableEmploye>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PageResponse<ResponsableEmploye>>(this.baseUrl, {
      params,
      withCredentials: true,
    });
  }

  getEmployeById(id: number): Observable<ResponsableEmploye> {
    return this.http.get<ResponsableEmploye>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }
}
