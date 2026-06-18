import { Role } from '../enums/role.enum';

export interface AuthUser {
  id: number;
  email: string;
  role: Role;
  redirectUrl?: string;
  employeId?: number | null;
  nom?: string | null;
  prenom?: string | null;
  matricule?: string | null;
  departementId?: number | null;
  departementNom?: string | null;
}
