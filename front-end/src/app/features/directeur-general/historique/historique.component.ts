import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface HistoriqueValidation {
  reference: string;
  employe: string;
  departement: string;
  type: 'Congé' | 'Rattrapage';
  dateValidation: string;
  dateDebut: string;
  dateFin: string;
  statut: 'Validée DG' | 'Refusée';
}

@Component({
  selector: 'app-directeur-general-historique',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './historique.component.html',
  styleUrls: ['./historique.component.scss'],
})
export class DirecteurGeneralHistoriqueComponent {
  searchTerm = '';
  selectedType = 'Tous';

  validations: HistoriqueValidation[] = [
    { reference: 'DEM-2026-022', employe: 'Ahmed Benali', departement: 'Informatique', type: 'Congé', dateValidation: '14/05/2026', dateDebut: '20/05/2026', dateFin: '23/05/2026', statut: 'Validée DG' },
    { reference: 'DEM-2026-018', employe: 'Nadia El Fassi', departement: 'Ressources Humaines', type: 'Rattrapage', dateValidation: '09/05/2026', dateDebut: '12/05/2026', dateFin: '12/05/2026', statut: 'Validée DG' },
    { reference: 'DEM-2026-011', employe: 'Youssef Idrissi', departement: 'Commercial', type: 'Congé', dateValidation: '02/05/2026', dateDebut: '18/05/2026', dateFin: '21/05/2026', statut: 'Refusée' },
  ];

  get types(): string[] {
    return ['Tous', 'Congé', 'Rattrapage'];
  }

  get validationsFiltrees(): HistoriqueValidation[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.validations.filter(validation => {
      const matchSearch = !search || validation.employe.toLowerCase().includes(search);
      const matchType = this.selectedType === 'Tous' || validation.type === this.selectedType;
      return matchSearch && matchType;
    });
  }
}
