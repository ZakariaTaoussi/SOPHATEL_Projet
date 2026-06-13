import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { CreateEmployeRequest, Employe, UpdateEmployeRequest } from '../models/employe.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class EmployeAdminService {
  private readonly baseUrl = apiUrl('/api/admin/employes');

  constructor(private readonly http: HttpClient) {}

  creerEmploye(request: CreateEmployeRequest): Observable<Employe> {
    return this.http.post<Employe>(this.baseUrl, request, { withCredentials: true });
  }

  modifierEmploye(id: number, request: UpdateEmployeRequest): Observable<Employe> {
    return this.http.put<Employe>(`${this.baseUrl}/${id}`, request, { withCredentials: true });
  }

  supprimerEmploye(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }

  consulterEmploye(id: number): Observable<Employe> {
    return this.http.get<Employe>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }

  consulterEmployes(page = 0, size = 3, search = ''): Observable<PageResponse<Employe>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<PageResponse<Employe>>(this.baseUrl, { params, withCredentials: true });
  }
}
