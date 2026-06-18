import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { Role } from '../enums/role.enum';
import { ProfilResponse, ProfilUpdateRequest } from '../models/profil.model';

@Injectable({ providedIn: 'root' })
export class ProfilService {
  constructor(private readonly http: HttpClient) {}

  getProfil(role: Role | string): Observable<ProfilResponse> {
    return this.http.get<ProfilResponse>(this.profilUrl(role), { withCredentials: true });
  }

  updateProfil(role: Role | string, payload: ProfilUpdateRequest): Observable<ProfilResponse> {
    return this.http.put<ProfilResponse>(this.profilUrl(role), payload, { withCredentials: true });
  }

  private profilUrl(role: Role | string): string {
    switch (role) {
      case Role.EMPLOYE:
      case 'EMPLOYE':
        return apiUrl('/api/employe/profil');
      case Role.RH:
      case 'RH':
        return apiUrl('/api/rh/profil');
      case Role.RESPONSABLE:
      case 'RESPONSABLE':
        return apiUrl('/api/responsable/profil');
      case Role.DIRECTEUR_GENERAL:
      case 'DIRECTEUR_GENERAL':
        return apiUrl('/api/directeur-general/profil');
      default:
        throw new Error('Vous n avez pas le droit d acceder a ce profil');
    }
  }
}
