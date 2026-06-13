import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { CreateDepartementRequest, Departement, UpdateDepartementRequest } from '../models/departement.model';

@Injectable({ providedIn: 'root' })
export class DepartementService {
  private readonly baseUrl = apiUrl('/api/admin/departements');

  constructor(private readonly http: HttpClient) {}

  creerDepartement(request: CreateDepartementRequest): Observable<Departement> {
    return this.http.post<Departement>(this.baseUrl, request, { withCredentials: true });
  }

  modifierDepartement(id: number, request: UpdateDepartementRequest): Observable<Departement> {
    return this.http.put<Departement>(`${this.baseUrl}/${id}`, request, { withCredentials: true });
  }

  supprimerDepartement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }

  consulterDepartements(): Observable<Departement[]> {
    return this.http.get<Departement[]>(this.baseUrl, { withCredentials: true });
  }

  consulterDepartement(id: number): Observable<Departement> {
    return this.http.get<Departement>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }
}
