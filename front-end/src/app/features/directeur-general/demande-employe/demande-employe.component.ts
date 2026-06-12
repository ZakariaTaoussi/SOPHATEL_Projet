import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

type StatutDemande = 'Validé_Responsable' | 'Validée DG' | 'Refusée';

interface DemandeEmploye {
  reference: string;
  employe: string;
  matricule: string;
  departement: string;
  type: 'Congé' | 'Rattrapage';
  dateDepot: string;
  dateDebut: string;
  dateFin: string;
  duree: number;
  statut: StatutDemande;
  commentaire?: string;
}

@Component({
  selector: 'app-directeur-general-demande-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demande-employe.component.html',
  styleUrls: ['./demande-employe.component.scss'],
})
export class DirecteurGeneralDemandeEmployeComponent {
  searchTerm = '';
  selectedType = 'Tous';
  selectedDepartement = 'Tous';

  demandes: DemandeEmploye[] = [
    { reference: 'DEM-2026-041', employe: 'Ahmed Benali', matricule: 'EMP-0042', departement: 'Informatique', type: 'Congé', dateDepot: '12/04/2026', dateDebut: '22/04/2026', dateFin: '26/04/2026', duree: 5, statut: 'Validé_Responsable' },
    { reference: 'DEM-2026-038', employe: 'Lina Mansouri', matricule: 'EMP-0057', departement: 'Finance', type: 'Rattrapage', dateDepot: '08/04/2026', dateDebut: '15/04/2026', dateFin: '15/04/2026', duree: 1, statut: 'Validé_Responsable' },
    { reference: 'DEM-2026-029', employe: 'Youssef Idrissi', matricule: 'EMP-0031', departement: 'Commercial', type: 'Congé', dateDepot: '21/03/2026', dateDebut: '02/05/2026', dateFin: '09/05/2026', duree: 6, statut: 'Validé_Responsable' },
  ];

  get types(): string[] {
    return ['Tous', 'Congé', 'Rattrapage'];
  }

  get departements(): string[] {
    return ['Tous', ...Array.from(new Set(this.demandes.map(demande => demande.departement))).sort()];
  }

  get demandesFiltrees(): DemandeEmploye[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.demandes.filter(demande => {
      const matchSearch = !search || demande.employe.toLowerCase().includes(search);
      const matchType = this.selectedType === 'Tous' || demande.type === this.selectedType;
      const matchDepartement = this.selectedDepartement === 'Tous' || demande.departement === this.selectedDepartement;
      return matchSearch && matchType && matchDepartement;
    });
  }

  valider(demande: DemandeEmploye): void {
    if (demande.statut !== 'Validé_Responsable') {
      return;
    }

    demande.statut = 'Validée DG';
    demande.commentaire = 'Demande validée par le Directeur Général.';
  }

  refuser(demande: DemandeEmploye): void {
    if (demande.statut !== 'Validé_Responsable') {
      return;
    }

    demande.statut = 'Refusée';
    demande.commentaire = 'Demande refusée par le Directeur Général.';
  }
}
