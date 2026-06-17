export type TypeDemande = 'CONGE' | 'ABSENCE';

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
  status: StatusDemande;
  joursDeduits: number;
  createdAt: string;
  updatedAt: string;
}

export interface DemandeCongeCreateRequest {
  dateDebutEmp: string;
  dateFinEmp: string;
  typeDemande: TypeDemande;
}

export interface DemandeCongeUpdateRequest {
  dateDebutEmp: string;
  dateFinEmp: string;
  typeDemande: TypeDemande;
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
  status: StatusDemande;
  joursDeduits: number;
  createdAt: string;
  updatedAt: string;
}

export interface ResponsableValidationDemandeRequest {
  dateDebutResp?: string | null;
  dateFinResp?: string | null;
}

export interface SoldeConge {
  id: number;
  empId: number;
  annee: number;
  soldeActuel: number;
  soldeTotal: number;
}
