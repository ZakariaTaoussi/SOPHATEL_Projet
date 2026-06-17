import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { finalize, Subscription } from 'rxjs';
import { DemandeConge, DemandeCongeUpdateRequest, StatusDemande, TypeDemande } from '../../../core/models/demande-conge.model';
import { DemandeCongeService } from '../../../core/services/demande-conge.service';
import { NotificationService } from '../../../core/services/notification.service';

type DemandeForm = {
  typeDemande: TypeDemande;
  dateDebutEmp: string;
  dateFinEmp: string;
};

@Component({
  selector: 'app-mes-demandes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-demandes.component.html',
  styleUrls: ['./mes-demandes.component.scss'],
})
export class MesDemandesComponent implements OnInit, OnDestroy {
  demandes: DemandeConge[] = [];
  demandeAImprimer?: DemandeConge;
  demandeEnModification?: DemandeConge;
  demandeForm: DemandeForm = {
    typeDemande: 'CONGE',
    dateDebutEmp: '',
    dateFinEmp: '',
  };

  selectedType: TypeDemande | 'Tous' = 'Tous';
  selectedStatus: StatusDemande | 'Tous' = 'Tous';
  loading = false;
  errorMessage = '';
  private readonly subscriptions = new Subscription();

  types: Array<TypeDemande | 'Tous'> = ['Tous', 'CONGE', 'ABSENCE'];
  editTypes: TypeDemande[] = ['CONGE', 'ABSENCE'];
  statuses: Array<StatusDemande | 'Tous'> = [
    'Tous',
    'BROUILLON',
    'VALIDE_EMPLOYE',
    'VALIDE_RESPONSABLE',
    'VALIDE_DG',
    'MODIFICATION_EMPLOYE',
    'MODIFICATION_RESPONSABLE',
    'MODIFICATION_DG',
    'ANNULE',
    'REFUSE_RESPONSABLE',
    'REFUSE_DG',
  ];

  constructor(
    private readonly demandeCongeService: DemandeCongeService,
    private readonly notificationService: NotificationService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.demandeCongeService.mesDemandes$.subscribe(demandes => {
        this.demandes = demandes;
        this.cdr.detectChanges();
      })
    );
    this.loadDemandes();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadDemandes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.demandeCongeService.getMesDemandes().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: demandes => {
        this.loading = false;
        this.demandes = demandes;
        this.cdr.detectChanges();
      },
      error: error => {
        this.handleError(error);
        this.cdr.detectChanges();
      },
    });
  }

  setType(value: string) {
    this.selectedType = value as TypeDemande | 'Tous';
    this.cdr.detectChanges();
  }

  setStatus(value: string) {
    this.selectedStatus = value as StatusDemande | 'Tous';
    this.cdr.detectChanges();
  }

  get filteredDemandes(): DemandeConge[] {
    return this.demandes.filter(d => {
      const matchType = this.selectedType === 'Tous' || d.typeDemande === this.selectedType;
      const matchStatus = this.selectedStatus === 'Tous' || d.status === this.selectedStatus;
      return matchType && matchStatus;
    });
  }

  getStatutLabel(statut: StatusDemande | 'Tous'): string {
    const labels: Record<StatusDemande | 'Tous', string> = {
      Tous: 'Tous',
      BROUILLON: 'Brouillon',
      VALIDE_EMPLOYE: 'Soumise',
      VALIDE_RESPONSABLE: 'Validee responsable',
      VALIDE_DG: 'Validee DG',
      MODIFICATION_EMPLOYE: 'Modification employe',
      MODIFICATION_RESPONSABLE: 'Modification responsable',
      MODIFICATION_DG: 'Modification DG',
      ANNULE: 'Annulee',
      REFUSE_RESPONSABLE: 'Refusee responsable',
      REFUSE_DG: 'Refusee DG',
    };

    return labels[statut];
  }

  getStatutClass(statut: StatusDemande): string {
    const classes: Record<StatusDemande, string> = {
      BROUILLON: 'draft',
      VALIDE_EMPLOYE: 'pending',
      VALIDE_RESPONSABLE: 'responsable-approved',
      VALIDE_DG: 'approved',
      MODIFICATION_EMPLOYE: 'draft',
      MODIFICATION_RESPONSABLE: 'pending',
      MODIFICATION_DG: 'pending',
      ANNULE: 'cancelled',
      REFUSE_RESPONSABLE: 'rejected',
      REFUSE_DG: 'rejected',
    };

    return classes[statut];
  }

  peutSubmit(demande: DemandeConge): boolean {
    return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_EMPLOYE';
  }

  peutModifierDirect(demande: DemandeConge): boolean {
    return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_EMPLOYE';
  }

  peutDemanderModification(demande: DemandeConge): boolean {
    return demande.status === 'VALIDE_EMPLOYE';
  }

  peutAnnuler(demande: DemandeConge): boolean {
    return demande.status !== 'ANNULE';
  }

  peutImprimer(demande: DemandeConge): boolean {
    return demande.status === 'VALIDE_DG';
  }

  imprimerDemande(demande: DemandeConge): void {
    if (!this.peutImprimer(demande)) {
      return;
    }

    this.demandeAImprimer = demande;
    setTimeout(() => window.print());
  }

  ouvrirModification(demande: DemandeConge): void {
    if (!this.peutModifierDirect(demande)) {
      return;
    }

    this.demandeEnModification = demande;
    this.demandeForm = {
      typeDemande: demande.typeDemande,
      dateDebutEmp: demande.dateDebutEmp,
      dateFinEmp: demande.dateFinEmp,
    };
  }

  fermerModification(): void {
    this.demandeEnModification = undefined;
  }

  enregistrerModification(): void {
    if (!this.demandeEnModification) {
      return;
    }

    const payload: DemandeCongeUpdateRequest = {
      typeDemande: this.demandeForm.typeDemande,
      dateDebutEmp: this.demandeForm.dateDebutEmp,
      dateFinEmp: this.demandeForm.dateFinEmp,
    };

    this.demandeCongeService.modifierDemande(this.demandeEnModification.id, payload).subscribe({
      next: demande => {
        this.demandeCongeService.refreshMesDemandes();
        this.loadDemandes();
        this.notificationService.add(`Demande DEM-${demande.id} modifiee.`, 'info');
        this.fermerModification();
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  submitDemande(demande: DemandeConge): void {
    if (!this.peutSubmit(demande)) {
      return;
    }

    this.demandeCongeService.submitDemande(demande.id).subscribe({
      next: updated => {
        this.loadDemandes();
        this.notificationService.add(`Demande DEM-${updated.id} soumise.`, 'success');
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  demanderModification(demande: DemandeConge): void {
    if (!this.peutDemanderModification(demande)) {
      return;
    }

    this.demandeCongeService.passerEnModification(demande.id).subscribe({
      next: updated => {
        this.loadDemandes();
        this.notificationService.add(`Demande DEM-${updated.id} repassee en modification.`, 'info');
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  annulerDemande(demande: DemandeConge): void {
    if (!this.peutAnnuler(demande)) {
      return;
    }

    this.demandeCongeService.annulerDemande(demande.id).subscribe({
      next: updated => {
        this.loadDemandes();
        this.notificationService.add(`Demande DEM-${updated.id} annulee.`, 'warning');
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  formatDate(value?: string | null): string {
    if (!value) {
      return '-';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(value));
  }

  private handleError(error: unknown): void {
    console.error('Erreur demande conge', error);
    if (error instanceof HttpErrorResponse) {
      this.errorMessage = typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? 'Une erreur est survenue.';
      return;
    }

    this.errorMessage = 'Une erreur est survenue.';
  }
}
