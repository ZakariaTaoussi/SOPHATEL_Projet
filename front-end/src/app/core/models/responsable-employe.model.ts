export interface ResponsableEmploye {
  id: number;
  utilisateurId?: number | null;
  nom?: string | null;
  prenom?: string | null;
  email?: string | null;
  role?: string | null;
  matricule?: string | null;
  departementId?: number | null;
  departementNom?: string | null;
  dateEmbauche?: string | null;
  soldeActuel?: number | null;
  soldeTotal?: number | null;
  anneeSolde?: number | null;
}
