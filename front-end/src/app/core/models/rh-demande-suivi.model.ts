export interface RhDemandeSuivi {
  id: number;
  reference: string | null;
  employeId: number | null;
  utilisateurId: number | null;
  employeNomComplet: string | null;
  matricule: string | null;
  email: string | null;
  departementId: number | null;
  departementNom: string | null;
  typeDemande: 'CONGE' | 'ABSENCE' | string | null;
  natureConge: string | null;
  status: string | null;
  dateDebutDg: string | null;
  dateFinDg: string | null;
  joursDeduits: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface RhDepartement {
  id: number;
  nom: string;
}
