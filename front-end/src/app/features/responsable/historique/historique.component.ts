import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface HistoriqueDemande {
  id: string;
  employe: string;
  type: 'Congé' | 'Rattrapage';
  dateDebut: string;
  dateFin: string;
  duree: number;
  annee: number;
  statut: 'Validée DG' | 'Refusée';
}

@Component({
  selector: 'app-responsable-historique',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './historique.component.html',
  styleUrls: ['./historique.component.scss'],
})
export class ResponsableHistoriqueComponent {
  selectedYear = 'Toutes';
  selectedType = 'Tous';
  searchTerm = '';

  demandes: HistoriqueDemande[] = [
    { id: 'HIS-001', employe: 'Ahmed Benali', type: 'Congé', dateDebut: '12/05/2026', dateFin: '14/05/2026', duree: 3, annee: 2026, statut: 'Validée DG' },
    { id: 'HIS-002', employe: 'Lina Mansouri', type: 'Rattrapage', dateDebut: '03/06/2026', dateFin: '03/06/2026', duree: 1, annee: 2026, statut: 'Validée DG' },
    { id: 'HIS-003', employe: 'Youssef Idrissi', type: 'Congé', dateDebut: '22/07/2026', dateFin: '28/07/2026', duree: 5, annee: 2026, statut: 'Refusée' },
    { id: 'HIS-004', employe: 'Nadia El Fassi', type: 'Congé', dateDebut: '20/12/2025', dateFin: '24/12/2025', duree: 5, annee: 2025, statut: 'Validée DG' },
  ];

  get years(): Array<number | 'Toutes'> {
    return ['Toutes', ...Array.from(new Set(this.demandes.map(d => d.annee))).sort((a, b) => b - a)];
  }

  get types(): string[] {
    return ['Tous', 'Congé', 'Rattrapage'];
  }

  get demandesFiltrees(): HistoriqueDemande[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.demandes.filter(demande => {
      const matchYear = this.selectedYear === 'Toutes' || demande.annee === Number(this.selectedYear);
      const matchType = this.selectedType === 'Tous' || demande.type === this.selectedType;
      const matchSearch = !search || demande.employe.toLowerCase().includes(search);
      return matchYear && matchType && matchSearch;
    });
  }
}
