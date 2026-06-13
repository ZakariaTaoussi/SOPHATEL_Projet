export interface JourCalendrier {
  id: number;
  date: string;
  jourFerieId: number | null;
  jourFerieNom: string | null;
  description: string | null;
}
