import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-directeur-general-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DirecteurGeneralDashboardComponent {
  stats = [
    { label: 'Demandes à valider', value: '7', unit: 'demandes', color: 'navy', icon: 'clock' },
    { label: 'Absences déclarées', value: '18', unit: 'absences', color: 'gold', icon: 'calendar' },
    { label: 'Départements suivis', value: '6', unit: 'départements', color: 'slate', icon: 'grid' },
    { label: 'Demandes refusées', value: '3', unit: 'demandes', color: 'teal', icon: 'refresh' },
  ];

  recentDemandes = [
    { type: 'Congé', dateDebut: '15/07/2026', dateFin: '25/07/2026', statut: 'En attente', statutClass: 'pending' },
    { type: 'Rattrapage', dateDebut: '03/06/2026', dateFin: '03/06/2026', statut: 'Validée DG', statutClass: 'approved' },
    { type: 'Congé', dateDebut: '12/05/2026', dateFin: '14/05/2026', statut: 'Validée DG', statutClass: 'approved' },
    { type: 'Congé', dateDebut: '02/04/2026', dateFin: '04/04/2026', statut: 'Refusée', statutClass: 'rejected' },
  ];

}
