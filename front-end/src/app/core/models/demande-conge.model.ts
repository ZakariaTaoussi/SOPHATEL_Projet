export type TypeDemande = 'CONGE' | 'ABSENCE';

export enum NatureConge {
  ANNUEL = 'ANNUEL',
  MALADIE = 'MALADIE',
  MATERNITE = 'MATERNITE',
  MISE_EN_DISPONIBILITE = 'MISE_EN_DISPONIBILITE',
}

export type StatusDemande =
  | 'BROUILLON'
  | 'VALIDE_EMPLOYE'
  | 'VALIDE_RESPONSABLE'
  | 'VALIDE_DG'
  | 'MODIFICATION_EMPLOYE'
  | 'MODIFICATION_RESPONSABLE'
  | 'MODIFICATION_DG'
  | 'ANNULE'
  | 'REFUSE_RESPONSABLE'
  | 'REFUSE_DG';

export interface DemandeConge {
  id: number;
  empId: number;
  employeNomComplet?: string;
  departementId?: number | null;
  departementNom?: string | null;
  dateDebutEmp: string;
  dateFinEmp: string;
  dateDebutResp?: string | null;
  dateFinResp?: string | null;
  dateDebutDg?: string | null;
  dateFinDg?: string | null;
  typeDemande: TypeDemande;
  natureConge?: NatureConge | null;
  status: StatusDemande;
  joursDeduits: number;
  createdAt: string;
  updatedAt: string;
}

export interface DemandeCongeCreateRequest {
  dateDebutEmp: string;
  dateFinEmp: string;
  typeDemande: TypeDemande;
  natureConge?: NatureConge | null;
}

export interface DemandeCongeUpdateRequest {
  dateDebutEmp: string;
  dateFinEmp: string;
  typeDemande: TypeDemande;
  natureConge?: NatureConge | null;
}

export interface ResponsableDemande {
  id: number;
  empId: number;
  employeNomComplet: string;
  departementId: number | null;
  departementNom: string | null;
  dateDebutEmp: string;
  dateFinEmp: string;
  dateDebutResp: string | null;
  dateFinResp: string | null;
  dateDebutDg: string | null;
  dateFinDg: string | null;
  typeDemande: TypeDemande;
  natureConge?: NatureConge | null;
  status: StatusDemande;
  joursDeduits: number;
  createdAt: string;
  updatedAt: string;
}

export interface ResponsableValidationDemandeRequest {
  dateDebutResp?: string | null;
  dateFinResp?: string | null;
}

export interface DirecteurGeneralDemande {
  id: number;
  empId: number;
  employeNomComplet: string;
  departementId: number | null;
  departementNom: string | null;
  responsableNomComplet: string | null;
  dateDebutEmp: string;
  dateFinEmp: string;
  dateDebutResp: string | null;
  dateFinResp: string | null;
  dateDebutDg: string | null;
  dateFinDg: string | null;
  typeDemande: TypeDemande;
  natureConge?: NatureConge | null;
  status: StatusDemande;
  joursDeduits: number;
  createdAt: string;
  updatedAt: string;
}

export interface DirecteurGeneralValidationDemandeRequest {
  dateDebutDg?: string | null;
  dateFinDg?: string | null;
}

export interface SoldeConge {
  id: number;
  empId: number;
  annee: number;
  soldeActuel: number;
  soldeTotal: number;
}
