import { Injectable, signal } from '@angular/core';
import { Role } from '../enums/role.enum';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // mock : changer la valeur pour tester
  readonly role = signal<Role>(Role.RH);

  setRole(r: Role) { this.role.set(r); }
  logout() { this.role.set(Role.RH); }
}
