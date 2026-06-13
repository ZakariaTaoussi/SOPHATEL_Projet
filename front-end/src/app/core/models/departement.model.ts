export interface Departement {
  id: number;
  nom: string;
  responsableId: number | null;
  responsableNomComplet: string | null;
  nombreEmployes: number;
}

export interface CreateDepartementRequest {
  nom: string;
}

export type UpdateDepartementRequest = CreateDepartementRequest;
