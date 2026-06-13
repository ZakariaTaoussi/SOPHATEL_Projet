import { Role } from '../enums/role.enum';

export type StatutEmploye = 'ACTIF' | 'INACTIF';

export interface Employe {
  id: number;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  departementId: number | null;
  departementNom: string | null;
  statut: StatutEmploye;
}

export interface CreateEmployeRequest {
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  password: string;
  role: Role;
  departementId: number | null;
  statut: StatutEmploye;
}

export type UpdateEmployeRequest = CreateEmployeRequest;
