import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-responsable-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class ResponsableDashboardComponent {
  stats = [
    { label: 'Jours de congé restants', value: '12', unit: 'jours', color: 'navy', icon: 'sun' },
    { label: 'Demandes en attente', value: '2', unit: 'demandes', color: 'gold', icon: 'clock' },
    { label: 'Absences ce mois', value: '1', unit: 'jour', color: 'slate', icon: 'calendar' },
    { label: 'Demandes refusées', value: '1', unit: 'demande', color: 'teal', icon: 'refresh' },
  ];

  recentDemandes = [
    { type: 'Congé', dateDebut: '15/07/2025', dateFin: '25/07/2025', statut: 'En attente', statutClass: 'pending' },
    { type: 'Rattrapage', dateDebut: '03/06/2025', dateFin: '03/06/2025', statut: 'Validée DG', statutClass: 'approved' },
    { type: 'Congé', dateDebut: '12/05/2025', dateFin: '14/05/2025', statut: 'Validée DG', statutClass: 'approved' },
    { type: 'Congé', dateDebut: '02/04/2025', dateFin: '04/04/2025', statut: 'Refusée', statutClass: 'rejected' },
  ];

  workflowSteps = [
    { order: 1, title: 'Création & soumission', description: "L'employé crée et soumet sa demande", color: 'blue' },
    { order: 2, title: 'Validation Responsable', description: 'Signature du responsable hiérarchique', color: 'cyan' },
    { order: 3, title: 'Validation DG', description: 'Signature finale du Directeur Général', color: 'green' },
    { order: 4, title: 'Notification & calcul', description: 'Mise à jour du reliquat et notification', color: 'purple' },
  ];
}
