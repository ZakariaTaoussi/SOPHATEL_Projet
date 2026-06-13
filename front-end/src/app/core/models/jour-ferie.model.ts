export interface JourFerie {
  id: number;
  nom: string;
  dateDebut: string;
  dateFin: string;
  description: string | null;
}

export interface CreateJourFerieRequest {
  nom: string;
  dateDebut: string;
  dateFin: string;
  description: string;
}

export type UpdateJourFerieRequest = CreateJourFerieRequest;
