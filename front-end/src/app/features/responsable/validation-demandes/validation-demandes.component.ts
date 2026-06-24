import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  NatureConge,
  ResponsableDemande,
  ResponsableValidationDemandeRequest,
  StatusDemande,
} from '../../../core/models/demande-conge.model';
import { ResponsableDemandeService } from '../../../core/services/responsable-demande.service';

type ModePage = 'a-valider' | 'absences-a-valider' | 'validees';

@Component({
  selector: 'app-responsable-validation-demandes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './validation-demandes.component.html',
  styleUrls: ['./validation-demandes.component.scss'],
})
export class ResponsableValidationDemandesComponent implements OnInit {
  demandes: ResponsableDemande[] = [];
  filteredDemandes: ResponsableDemande[] = [];
  selectedType = 'Tous';
  selectedStatus = 'Tous';
  mode: ModePage = 'a-valider';
  currentPage = 1;
  readonly pageSize = 4;
  loading = false;
  actionLoadingId: number | null = null;
  actionLoadingType: 'valider' | 'refuser' | 'modifier' | null = null;
  errorMessage = '';
  successMessage = '';
  demandeEnModification: ResponsableDemande | null = null;
  modificationForm = {
    dateDebutResp: '',
    dateFinResp: '',
  };

  readonly statusOptions: Array<StatusDemande | 'Tous'> = [
    'Tous',
    'VALIDE_EMPLOYE',
    'VALIDE_RESPONSABLE',
    'REFUSE_RESPONSABLE',
    'MODIFICATION_RESPONSABLE',
  ];

  readonly naturesConge = [
    { value: NatureConge.ANNUEL, label: 'Annuel' },
    { value: NatureConge.MALADIE, label: 'Maladie' },
    { value: NatureConge.MATERNITE, label: 'Maternite' },
    { value: NatureConge.MISE_EN_DISPONIBILITE, label: 'Mise en disponibilite' },
  ];

  constructor(
    private readonly responsableDemandeService: ResponsableDemandeService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.syncModeFromUrl();
    this.loadDemandes();
  }

  get pageTitle(): string {
    if (this.mode === 'validees') {
      return 'Demandes validees';
    }
    return this.mode === 'absences-a-valider' ? 'Absences a valider' : 'Demandes a valider';
  }

  get pageDescription(): string {
    if (this.mode === 'validees') {
      return 'Demandes deja traitees par le responsable.';
    }
    return this.mode === 'absences-a-valider'
      ? 'Absences soumises par les employes de votre departement.'
      : 'Demandes soumises par les employes de votre departement.';
  }

  get emptyMessage(): string {
    return this.mode === 'validees' ? 'Aucune demande traitee' : 'Aucune demande a valider';
  }

  get pagedDemandes(): ResponsableDemande[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDemandes.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDemandes.length / this.pageSize));
  }

  loadDemandes(): void {
    this.loading = true;
    this.errorMessage = '';

    const request$ = this.mode === 'validees'
      ? this.responsableDemandeService.getDemandesValidees()
      : this.isAbsencesMode
      ? this.responsableDemandeService.getAbsencesAValider()
      : this.responsableDemandeService.getDemandesAValider();

    request$.pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: demandes => {
        this.demandes = [...demandes];
        this.applyFilters();
      },
      error: error => {
        console.error('Erreur chargement demandes responsable', error);
        this.demandes = [];
        this.filteredDemandes = [];
        this.currentPage = 1;
        this.errorMessage = this.getErrorMessage(error, 'Erreur lors du chargement des demandes');
      },
    });
  }

  applyFilters(): void {
    const result = this.demandes.filter(demande => {
      const matchType = this.selectedType === 'Tous' || demande.typeDemande === this.selectedType;
      const matchStatus = this.selectedStatus === 'Tous' || demande.status === this.selectedStatus;
      return this.statusMatchesMode(demande.status) && matchType && matchStatus;
    });

    this.filteredDemandes = [...result];
    this.ensureValidPage();
    this.cdr.markForCheck();
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFilters();
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

  validerSansModification(demande: ResponsableDemande): void {
    const payload: ResponsableValidationDemandeRequest = {
      dateDebutResp: null,
      dateFinResp: null,
    };

    this.executerAction(
      demande.id,
      'valider',
      demande.typeDemande === 'ABSENCE' ? 'Absence validee par le responsable.' : 'Demande validee par le responsable.',
      () => demande.typeDemande === 'ABSENCE'
        ? this.responsableDemandeService.validerAbsence(demande.id, payload)
        : this.responsableDemandeService.validerDemande(demande.id, payload),
      'Erreur lors de la validation'
    );
  }

  ouvrirModification(demande: ResponsableDemande): void {
    this.clearMessages();
    this.demandeEnModification = demande;
    this.modificationForm = {
      dateDebutResp: demande.dateDebutResp || demande.dateDebutEmp,
      dateFinResp: demande.dateFinResp || demande.dateFinEmp,
    };
    this.cdr.markForCheck();
  }

  fermerModification(): void {
    this.demandeEnModification = null;
    this.modificationForm = {
      dateDebutResp: '',
      dateFinResp: '',
    };
    this.cdr.markForCheck();
  }

  validerAvecModification(): void {
    if (!this.demandeEnModification) {
      return;
    }

    const demande = this.demandeEnModification;
    const payload: ResponsableValidationDemandeRequest = {
      dateDebutResp: this.modificationForm.dateDebutResp,
      dateFinResp: this.modificationForm.dateFinResp,
    };

    this.executerAction(
      demande.id,
      'modifier',
      demande.typeDemande === 'ABSENCE' ? 'Absence validee avec les dates responsable.' : 'Demande validee avec les dates responsable.',
      () => demande.typeDemande === 'ABSENCE'
        ? this.responsableDemandeService.validerAbsence(demande.id, payload)
        : this.responsableDemandeService.validerDemande(demande.id, payload),
      'Erreur lors de la validation avec modification',
      () => this.fermerModification()
    );
  }

  refuserDemande(demande: ResponsableDemande): void {
    if (!this.hasValidId(demande)) {
      this.errorMessage = 'Impossible de refuser : id de demande manquant';
      return;
    }

    this.executerAction(
      demande.id,
      'refuser',
      demande.typeDemande === 'ABSENCE' ? 'Absence refusee par le responsable.' : 'Demande refusee par le responsable.',
      () => demande.typeDemande === 'ABSENCE'
        ? this.responsableDemandeService.refuserAbsence(demande.id)
        : this.responsableDemandeService.refuserDemande(demande.id),
      'Erreur lors du refus'
    );
  }

  passerEnModificationResponsable(demande: ResponsableDemande): void {
    if (!this.peutPasserEnModificationResponsable(demande)) {
      return;
    }

    this.executerAction(
      demande.id,
      'modifier',
      'Demande passee en modification responsable.',
      () => this.responsableDemandeService.passerEnModificationResponsable(demande.id),
      'Erreur lors du passage en modification'
    );
  }

  peutValider(demande: ResponsableDemande): boolean {
    if (this.mode === 'validees') {
      return false;
    }
    return demande.status === 'VALIDE_EMPLOYE' || demande.status === 'MODIFICATION_RESPONSABLE';
  }

  peutModifierDates(demande: ResponsableDemande): boolean {
    if (this.mode === 'validees') {
      return false;
    }
    return demande.status === 'VALIDE_EMPLOYE' || demande.status === 'MODIFICATION_RESPONSABLE';
  }

  peutPasserEnModificationResponsable(demande: ResponsableDemande): boolean {
    return demande.status === 'VALIDE_RESPONSABLE';
  }

  peutRefuser(demande: ResponsableDemande): boolean {
    if (this.mode === 'validees') {
      return false;
    }
    return demande.status === 'VALIDE_EMPLOYE' || demande.status === 'MODIFICATION_RESPONSABLE';
  }

  peutImprimer(demande: ResponsableDemande): boolean {
    return demande.typeDemande === 'CONGE'
      && ['VALIDE_DG', 'ANNULE', 'REFUSE_RESPONSABLE', 'REFUSE_DG'].includes(demande.status);
  }

  imprimerDemande(demande: ResponsableDemande): void {
    if (this.peutImprimer(demande)) {
      this.router.navigate(['/demande-conge', demande.id, 'impression']);
    }
  }

  dureeDemande(demande: ResponsableDemande): number {
    if (demande.typeDemande === 'CONGE') {
      return demande.joursDeduits || 0;
    }
    const debut = new Date(demande.dateDebutEmp);
    const fin = new Date(demande.dateFinEmp);
    return Math.max(1, Math.round((fin.getTime() - debut.getTime()) / 86_400_000) + 1);
  }

  isActionLoading(demande: ResponsableDemande, type: 'valider' | 'refuser' | 'modifier'): boolean {
    return this.actionLoadingId === demande.id && this.actionLoadingType === type;
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

  getStatusLabel(status: StatusDemande): string {
    const labels: Record<StatusDemande, string> = {
      BROUILLON: 'Brouillon',
      VALIDE_EMPLOYE: 'Validee employe',
      VALIDE_RESPONSABLE: 'Validee responsable',
      VALIDE_DG: 'Validee DG',
      MODIFICATION_EMPLOYE: 'Modification employe',
      MODIFICATION_RESPONSABLE: 'Modification responsable',
      MODIFICATION_DG: 'Modification DG',
      ANNULE: 'Annulee',
      REFUSE_RESPONSABLE: 'Refusee responsable',
      REFUSE_DG: 'Refusee DG',
    };

    return labels[status] ?? status;
  }

  getNatureLabel(nature?: NatureConge | null): string {
    return this.naturesConge.find(option => option.value === nature)?.label ?? '-';
  }

  private hasValidId(demande: ResponsableDemande | null | undefined): demande is ResponsableDemande {
    return Number.isFinite(demande?.id) && Number(demande?.id) > 0;
  }

  private executerAction(
    demandeId: number,
    type: 'valider' | 'refuser' | 'modifier',
    successMessage: string,
    requestFactory: () => ReturnType<ResponsableDemandeService['validerDemande']>,
    fallbackError: string,
    afterSuccess?: () => void
  ): void {
    this.clearMessages();
    this.actionLoadingId = demandeId;
    this.actionLoadingType = type;

    requestFactory().pipe(
      finalize(() => {
        this.actionLoadingId = null;
        this.actionLoadingType = null;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: () => {
        this.successMessage = successMessage;
        afterSuccess?.();
        this.loadDemandes();
      },
      error: error => {
        console.error('Erreur action demande responsable', error);
        this.errorMessage = this.getErrorMessage(error, fallbackError);
      },
    });
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private get isAbsencesMode(): boolean {
    return this.router.url.includes('/absences-a-valider');
  }

  private syncModeFromUrl(): void {
    if (this.router.url.includes('/demandes-validees')) {
      this.mode = 'validees';
      return;
    }
    this.mode = this.router.url.includes('/absences-a-valider') ? 'absences-a-valider' : 'a-valider';
  }

  private statusMatchesMode(status: StatusDemande): boolean {
    if (this.mode === 'validees') {
      return ['VALIDE_RESPONSABLE', 'REFUSE_RESPONSABLE', 'MODIFICATION_RESPONSABLE'].includes(status);
    }
    return status === 'VALIDE_EMPLOYE';
  }

  private ensureValidPage(): void {
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
    if (this.currentPage < 1) {
      this.currentPage = 1;
    }
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error;
      }
      return error.error?.message || fallback;
    }
    if (error instanceof Error && error.message.trim()) {
      return error.message;
    }

    return fallback;
  }
}
