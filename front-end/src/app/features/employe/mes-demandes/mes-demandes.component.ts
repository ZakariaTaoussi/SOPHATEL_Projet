import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../../core/services/notification.service';

type StatutDemande = 'EN_ATTENTE' | 'VALIDEE_RESPONSABLE' | 'VALIDEE_DG' | 'ANNULE' | 'REFUSE';

type Demande = {
  id: string;
  type: 'Congé' | 'Rattrapage' | string;
  dateDepot: string;
  dateDebut: string;
  dateFin: string;
  duree: number;
  statut: StatutDemande;
  commentaire?: string;
};

type DemandeForm = {
  type: string;
  dateDebut: string;
  dateFin: string;
  commentaire: string;
};

@Component({
  selector: 'app-mes-demandes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-demandes.component.html',
  styleUrls: ['./mes-demandes.component.scss'],
})
export class MesDemandesComponent {
  constructor(private readonly notificationService: NotificationService) {}

  demandes: Demande[] = [
    { id: 'DEM-001', type: 'Rattrapage', dateDepot: '01/04/2025', dateDebut: '02/04/2025', dateFin: '04/04/2025', duree: 3, statut: 'VALIDEE_RESPONSABLE', commentaire: '' },
    { id: 'DEM-002', type: 'Congé', dateDepot: '11/05/2025', dateDebut: '12/05/2025', dateFin: '14/05/2025', duree: 3, statut: 'VALIDEE_DG', commentaire: '' },
    { id: 'DEM-003', type: 'Congé', dateDepot: '01/06/2025', dateDebut: '03/06/2025', dateFin: '03/06/2025', duree: 1, statut: 'VALIDEE_DG', commentaire: '' },
    { id: 'DEM-004', type: 'Congé', dateDepot: '10/07/2025', dateDebut: '15/07/2025', dateFin: '25/07/2025', duree: 11, statut: 'EN_ATTENTE', commentaire: '' },
    { id: 'DEM-005', type: 'Congé', dateDepot: '05/09/2025', dateDebut: '10/09/2025', dateFin: '10/09/2025', duree: 1, statut: 'REFUSE', commentaire: 'Manque de personnel ce jour.' },
  ];

  demandeAImprimer?: Demande;
  demandeEnModification?: Demande;
  demandeForm: DemandeForm = {
    type: 'Congé',
    dateDebut: '',
    dateFin: '',
    commentaire: '',
  };

  selectedType = 'Tous';
  selectedStatus: StatutDemande | 'Tous' = 'Tous';

  types = ['Tous', 'Congé', 'Rattrapage'];
  editTypes = ['Congé', 'Rattrapage'];
  statuses: Array<StatutDemande | 'Tous'> = ['Tous', 'EN_ATTENTE', 'VALIDEE_RESPONSABLE', 'VALIDEE_DG', 'ANNULE', 'REFUSE'];

  setType(value: string) { this.selectedType = value; }
  setStatus(value: string) { this.selectedStatus = value as StatutDemande | 'Tous'; }

  get filteredDemandes(): Demande[] {
    return this.demandes.filter(d => {
      const matchType = this.selectedType === 'Tous' || d.type === this.selectedType;
      const matchStatus = this.selectedStatus === 'Tous' || d.statut === this.selectedStatus;
      return matchType && matchStatus;
    });
  }

  getStatutLabel(statut: StatutDemande | 'Tous'): string {
    const labels: Record<StatutDemande | 'Tous', string> = {
      Tous: 'Tous',
      EN_ATTENTE: 'En attente',
      VALIDEE_RESPONSABLE: 'Validée responsable',
      VALIDEE_DG: 'Validée DG',
      ANNULE: 'Annulée',
      REFUSE: 'Refusée',
    };

    return labels[statut];
  }

  getStatutClass(statut: StatutDemande): string {
    const classes: Record<StatutDemande, string> = {
      EN_ATTENTE: 'pending',
      VALIDEE_RESPONSABLE: 'responsable-approved',
      VALIDEE_DG: 'approved',
      ANNULE: 'cancelled',
      REFUSE: 'rejected',
    };

    return classes[statut];
  }

  peutImprimer(demande: Demande): boolean {
    return demande.statut === 'VALIDEE_DG';
  }

  peutModifier(demande: Demande): boolean {
    return demande.statut === 'EN_ATTENTE';
  }

  imprimerDemande(demande: Demande): void {
    if (!this.peutImprimer(demande)) {
      return;
    }

    this.demandeAImprimer = demande;
    setTimeout(() => window.print());
  }

  ouvrirModification(demande: Demande): void {
    if (!this.peutModifier(demande)) {
      return;
    }

    this.demandeEnModification = demande;
    this.demandeForm = {
      type: demande.type,
      dateDebut: this.toInputDate(demande.dateDebut),
      dateFin: this.toInputDate(demande.dateFin),
      commentaire: demande.commentaire ?? '',
    };
  }

  fermerModification(): void {
    this.demandeEnModification = undefined;
  }

  enregistrerModification(): void {
    if (!this.demandeEnModification) {
      return;
    }

    this.demandeEnModification.type = this.demandeForm.type;
    this.demandeEnModification.dateDebut = this.toDisplayDate(this.demandeForm.dateDebut);
    this.demandeEnModification.dateFin = this.toDisplayDate(this.demandeForm.dateFin);
    this.demandeEnModification.commentaire = this.demandeForm.commentaire;
    this.demandeEnModification.duree = this.calculerDuree(this.demandeForm.dateDebut, this.demandeForm.dateFin);

    this.notificationService.add(`Votre demande ${this.demandeEnModification.id} a été modifiée.`, 'info');
    this.fermerModification();
  }

  annulerDemande(demande: Demande): void {
    if (!this.peutModifier(demande)) {
      return;
    }

    demande.statut = 'ANNULE';
    demande.commentaire = 'Demande annulée par l’employé.';
    this.notificationService.add(`Votre demande ${demande.id} a été annulée.`, 'warning');
  }

  private toInputDate(date: string): string {
    const [day, month, year] = date.split('/');
    return `${year}-${month}-${day}`;
  }

  private toDisplayDate(date: string): string {
    const [year, month, day] = date.split('-');
    return `${day}/${month}/${year}`;
  }

  private calculerDuree(dateDebut: string, dateFin: string): number {
    const debut = new Date(dateDebut);
    const fin = new Date(dateFin);
    const diff = fin.getTime() - debut.getTime();
    const days = Math.floor(diff / 86400000) + 1;
    return Math.max(days, 1);
  }
}
