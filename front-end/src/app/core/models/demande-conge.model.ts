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
  | 'REFUSE';

export interface DemandeConge {
  id: number;
  empId: number;
  employeNomComplet?: string;
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

export interface SoldeConge {
  id: number;
  empId: number;
  annee: number;
  soldeActuel: number;
  soldeTotal: number;
}
