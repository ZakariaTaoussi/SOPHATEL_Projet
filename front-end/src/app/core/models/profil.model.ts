export interface ProfilResponse {
  utilisateurId: number;
  employeId: number;
  nom: string;
  prenom: string;
  email: string;
  role: string;
  matricule: string;
  dateEmbauche?: string | null;
  departementId?: number | null;
  departementNom?: string | null;
  managerId?: number | null;
  managerNomComplet?: string | null;
  soldeActuel?: number | null;
  soldeTotal?: number | null;
  anneeSolde?: number | null;
}

export interface ProfilUpdateRequest {
  nom: string;
  prenom: string;
}
