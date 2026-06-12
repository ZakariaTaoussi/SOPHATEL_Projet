import { Role } from '../enums/role.enum';

export interface Utilisateur {
  id_user: number;
  nom: string;
  prenom: string;
  email: string;
  password?: string;
  role: Role;
}
