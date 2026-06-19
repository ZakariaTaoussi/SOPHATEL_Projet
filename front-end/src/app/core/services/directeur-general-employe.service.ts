import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { DirecteurGeneralEmploye } from '../models/directeur-general-employe.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class DirecteurGeneralEmployeService {
  private readonly baseUrl = apiUrl('/api/directeur-general/employes');

  constructor(private readonly http: HttpClient) {}

  getEmployes(
    page = 0,
    size = 4,
    search = '',
    role = '',
    departementId?: number | null
  ): Observable<PageResponse<DirecteurGeneralEmploye>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    if (role.trim()) {
      params = params.set('role', role.trim());
    }
    if (departementId !== null && departementId !== undefined) {
      params = params.set('departementId', departementId);
    }

    return this.http.get<PageResponse<DirecteurGeneralEmploye>>(this.baseUrl, {
      params,
      withCredentials: true,
    });
  }
}
