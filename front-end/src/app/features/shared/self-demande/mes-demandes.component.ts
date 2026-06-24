import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DemandeConge,
  DemandeCongeUpdateRequest,
  NatureConge,
  SoldeConge,
  StatusDemande,
  TypeDemande,
} from '../../../core/models/demande-conge.model';
import {
  SelfDemandeCongeService,
  SelfDemandeScope,
} from '../../../core/services/self-demande-conge.service';
import { NotificationService } from '../../../core/services/notification.service';

type DemandeForm = {
  typeDemande: TypeDemande;
  natureConge: NatureConge | null;
  dateDebutEmp: string;
  dateFinEmp: string;
};

type ActionName = 'submit' | 'modifier' | 'annuler' | 'save';

@Component({
  selector: 'app-self-mes-demandes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-demandes.component.html',
  styleUrls: ['../../employe/mes-demandes/mes-demandes.component.scss'],
})
export class SelfMesDemandesComponent implements OnInit {
  demandes: DemandeConge[] = [];
  filteredDemandes: DemandeConge[] = [];
  demandeAImprimer?: DemandeConge;
  demandeEnModification?: DemandeConge;
  demandeForm: DemandeForm = {
    typeDemande: 'CONGE',
    natureConge: null,
    dateDebutEmp: '',
    dateFinEmp: '',
  };

  solde?: SoldeConge;
  selectedStatus: StatusDemande | 'Tous' = 'Tous';
  currentPage = 1;
  readonly pageSize = 5;
  loading = false;
  actionLoadingId: number | null = null;
  actionLoadingName: ActionName | null = null;
  errorMessage = '';
  successMessage = '';

  editTypes: TypeDemande[] = ['CONGE'];
  naturesConge = [
    { value: NatureConge.ANNUEL, label: 'Annuel' },
    { value: NatureConge.MALADIE, label: 'Maladie' },
    { value: NatureConge.MATERNITE, label: 'Maternite' },
    { value: NatureConge.MISE_EN_DISPONIBILITE, label: 'Mise en disponibilite' },
  ];
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

  private readonly scope: SelfDemandeScope;

  constructor(
    private readonly demandeService: SelfDemandeCongeService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.scope = this.route.snapshot.data['scope'] as SelfDemandeScope;
  }

  ngOnInit(): void {
    this.loadDemandes();
    this.loadSolde();
  }

  loadDemandes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.demandeService.getMesDemandes(this.scope).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: demandes => {
        this.demandes = demandes.filter(demande => demande.typeDemande === 'CONGE');
        this.applyFilters();
        this.cdr.markForCheck();
      },
      error: error => {
        this.handleError(error, 'Erreur lors du chargement');
        this.demandes = [];
        this.filteredDemandes = [];
        this.cdr.markForCheck();
      },
    });
  }

  loadSolde(): void {
    this.demandeService.getSoldeConge(this.scope).subscribe({
      next: solde => {
        this.solde = solde;
        this.cdr.markForCheck();
      },
      error: error => this.handleError(error, 'Erreur lors du chargement du solde'),
    });
  }

  setStatus(value: string): void {
    this.selectedStatus = value as StatusDemande | 'Tous';
    this.currentPage = 1;
    this.applyFilters();
  }

  applyFilters(): void {
    const result = this.demandes.filter(demande => {
      const matchStatus = this.selectedStatus === 'Tous' || demande.status === this.selectedStatus;
      return demande.typeDemande === 'CONGE' && matchStatus;
    });
    this.filteredDemandes = [...result];
    this.ensureValidPage();
    this.cdr.markForCheck();
  }

  get pagedDemandes(): DemandeConge[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDemandes.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDemandes.length / this.pageSize));
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.cdr.markForCheck();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.cdr.markForCheck();
    }
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
    if (this.scope === 'responsable') {
      return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_RESPONSABLE';
    }
    if (this.scope === 'directeur-general') {
      return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_DG';
    }
    return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_EMPLOYE';
  }

  peutModifierDirect(demande: DemandeConge): boolean {
    if (this.scope === 'responsable') {
      return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_RESPONSABLE';
    }
    if (this.scope === 'directeur-general') {
      return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_DG';
    }
    return demande.status === 'BROUILLON' || demande.status === 'MODIFICATION_EMPLOYE';
  }

  peutDemanderModification(demande: DemandeConge): boolean {
    if (this.scope === 'responsable') {
      return demande.status === 'VALIDE_RESPONSABLE';
    }
    if (this.scope === 'directeur-general') {
      return demande.status === 'VALIDE_DG';
    }
    return demande.status === 'VALIDE_EMPLOYE';
  }

  peutAnnuler(demande: DemandeConge): boolean {
    if (this.scope === 'directeur-general') {
      return demande.status !== 'ANNULE' && this.dateDebutEffectiveFuture(demande);
    }
    if (this.scope === 'responsable') {
      return !['VALIDE_DG', 'MODIFICATION_DG', 'REFUSE_DG', 'ANNULE'].includes(demande.status);
    }
    return ![
      'VALIDE_DG',
      'MODIFICATION_DG',
      'REFUSE_RESPONSABLE',
      'REFUSE_DG',
      'ANNULE',
    ].includes(demande.status);
  }

  peutImprimer(demande: DemandeConge): boolean {
    return demande.typeDemande === 'CONGE'
      && ['VALIDE_DG', 'ANNULE', 'REFUSE_RESPONSABLE', 'REFUSE_DG'].includes(demande.status);
  }

  isActionLoading(demande: DemandeConge, action: ActionName): boolean {
    return this.actionLoadingId === demande.id && this.actionLoadingName === action;
  }

  imprimerDemande(demande: DemandeConge): void {
    if (!this.peutImprimer(demande)) {
      return;
    }

    this.router.navigate(['/demande-conge', demande.id, 'impression']);
  }

  ouvrirModification(demande: DemandeConge): void {
    if (!this.peutModifierDirect(demande)) {
      return;
    }

    this.demandeEnModification = demande;
    this.demandeForm = {
      typeDemande: 'CONGE',
      natureConge: demande.natureConge ?? null,
      dateDebutEmp: this.dateDebutEdition(demande),
      dateFinEmp: this.dateFinEdition(demande),
    };
  }

  fermerModification(): void {
    this.demandeEnModification = undefined;
  }

  enregistrerModification(): void {
    if (!this.demandeEnModification?.id) {
      this.errorMessage = 'Id de demande manquant';
      return;
    }
    if (this.demandeForm.typeDemande === 'CONGE' && !this.demandeForm.natureConge) {
      this.errorMessage = 'Veuillez choisir la nature du conge.';
      return;
    }

    const demandeId = this.demandeEnModification.id;
    const payload: DemandeCongeUpdateRequest = {
      typeDemande: this.demandeForm.typeDemande,
      natureConge: this.demandeForm.typeDemande === 'CONGE' ? this.demandeForm.natureConge : null,
      dateDebutEmp: this.demandeForm.dateDebutEmp,
      dateFinEmp: this.demandeForm.dateFinEmp,
    };

    this.actionLoadingId = demandeId;
    this.actionLoadingName = 'save';
    this.errorMessage = '';

    this.demandeService.modifierDemande(this.scope, demandeId, payload).pipe(
      finalize(() => {
        this.actionLoadingId = null;
        this.actionLoadingName = null;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: demande => this.afterAction(`Demande DEM-${demande.id} modifiee.`, 'info'),
      error: error => this.handleError(error, 'Erreur lors de la modification'),
    });
  }

  submitDemande(demande: DemandeConge): void {
    if (!demande.id) {
      this.errorMessage = 'Id de demande manquant';
      return;
    }
    if (!this.peutSubmit(demande)) {
      return;
    }

    this.runAction(demande, 'submit', () => this.demandeService.submitDemande(this.scope, demande.id), 'soumise', 'success');
  }

  demanderModification(demande: DemandeConge): void {
    if (!demande.id) {
      this.errorMessage = 'Id de demande manquant';
      return;
    }
    if (!this.peutDemanderModification(demande)) {
      return;
    }

    this.runAction(
      demande,
      'modifier',
      () => this.demandeService.passerEnModification(this.scope, demande.id),
      'repassee en modification',
      'info'
    );
  }

  annulerDemande(demande: DemandeConge): void {
    if (!demande.id) {
      this.errorMessage = 'Id de demande manquant';
      return;
    }
    if (!this.peutAnnuler(demande)) {
      return;
    }

    this.runAction(demande, 'annuler', () => this.demandeService.annulerDemande(this.scope, demande.id), 'annulee', 'warning');
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

  getNatureLabel(nature?: NatureConge | null): string {
    return this.naturesConge.find(option => option.value === nature)?.label ?? '-';
  }

  private runAction(
    demande: DemandeConge,
    action: ActionName,
    operation: () => ReturnType<SelfDemandeCongeService['submitDemande']>,
    label: string,
    kind: 'info' | 'success' | 'warning'
  ): void {
    this.actionLoadingId = demande.id;
    this.actionLoadingName = action;
    this.errorMessage = '';

    operation().pipe(
      finalize(() => {
        this.actionLoadingId = null;
        this.actionLoadingName = null;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: updated => this.afterAction(`Demande DEM-${updated.id} ${label}.`, kind),
      error: error => this.handleError(error, 'Erreur lors de l operation'),
    });
  }

  private afterAction(message: string, kind: 'info' | 'success' | 'warning'): void {
    this.successMessage = message;
    this.notificationService.add(message, kind);
    this.fermerModification();
    this.loadDemandes();
    this.loadSolde();
    this.cdr.markForCheck();
  }

  private handleError(error: unknown, fallback: string): void {
    if (error instanceof HttpErrorResponse) {
      this.errorMessage = typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? fallback;
    } else if (error instanceof Error) {
      this.errorMessage = error.message;
    } else {
      this.errorMessage = fallback;
    }
    this.cdr.markForCheck();
  }

  private dateDebutEdition(demande: DemandeConge): string {
    if (this.scope === 'responsable') {
      return demande.dateDebutResp ?? demande.dateDebutEmp;
    }
    if (this.scope === 'directeur-general') {
      return demande.dateDebutDg ?? demande.dateDebutEmp;
    }
    return demande.dateDebutEmp;
  }

  private dateFinEdition(demande: DemandeConge): string {
    if (this.scope === 'responsable') {
      return demande.dateFinResp ?? demande.dateFinEmp;
    }
    if (this.scope === 'directeur-general') {
      return demande.dateFinDg ?? demande.dateFinEmp;
    }
    return demande.dateFinEmp;
  }

  private dateDebutEffectiveFuture(demande: DemandeConge): boolean {
    const dateDebut = demande.dateDebutDg ?? demande.dateDebutResp ?? demande.dateDebutEmp;
    if (!dateDebut) {
      return false;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const effectiveDate = new Date(dateDebut);
    effectiveDate.setHours(0, 0, 0, 0);
    return effectiveDate > today;
  }

  private ensureValidPage(): void {
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
    if (this.currentPage < 1) {
      this.currentPage = 1;
    }
  }
}
