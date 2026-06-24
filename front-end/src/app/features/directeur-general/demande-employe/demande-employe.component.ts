import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, Observable } from 'rxjs';
import {
  DirecteurGeneralDemande,
  DirecteurGeneralValidationDemandeRequest,
  NatureConge,
  StatusDemande,
} from '../../../core/models/demande-conge.model';
import { DirecteurGeneralDemandeService } from '../../../core/services/directeur-general-demande.service';

type ModePage = 'a-valider' | 'absences-a-valider' | 'validees';
type ActionDg = 'valider' | 'refuser' | 'modifier';

@Component({
  selector: 'app-directeur-general-demande-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './demande-employe.component.html',
  styleUrls: ['./demande-employe.component.scss'],
})
export class DirecteurGeneralDemandeEmployeComponent implements OnInit {
  searchTerm = '';
  selectedType = 'Tous';
  selectedDepartement = 'Tous';

  mode: ModePage = 'a-valider';
  demandes: DirecteurGeneralDemande[] = [];
  demandesFiltrees: DirecteurGeneralDemande[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';
  actionLoadingId: number | null = null;
  actionLoadingType: ActionDg | null = null;

  demandeEnModification: DirecteurGeneralDemande | null = null;
  modificationForm: DirecteurGeneralValidationDemandeRequest = {
    dateDebutDg: null,
    dateFinDg: null,
  };

  constructor(
    private readonly directeurGeneralDemandeService: DirecteurGeneralDemandeService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.syncModeFromUrl();
    this.loadDemandes();
  }

  get isModeValidees(): boolean {
    return this.mode === 'validees';
  }

  get pageTitle(): string {
    if (this.mode === 'absences-a-valider') {
      return 'Absences a valider';
    }
    return this.isModeValidees ? 'Demandes validees DG' : 'Demandes a valider';
  }

  get pageDescription(): string {
    if (this.mode === 'absences-a-valider') {
      return 'Absences validees par les responsables et en attente de decision DG.';
    }
    return this.isModeValidees
      ? 'Demandes deja validees par le directeur general.'
      : 'Demandes validees par les responsables et en attente de decision DG.';
  }

  get tableTitle(): string {
    if (this.mode === 'absences-a-valider') {
      return 'Absences en attente DG';
    }
    return this.isModeValidees ? 'Demandes validees' : 'Demandes en attente DG';
  }

  get emptyMessage(): string {
    if (this.mode === 'absences-a-valider') {
      return 'Aucune absence a valider pour le directeur general.';
    }
    return this.isModeValidees
      ? 'Aucune demande validee par le directeur general.'
      : 'Aucune demande a valider pour le directeur general.';
  }

  get types(): string[] {
    return ['Tous', 'CONGE', 'ABSENCE'];
  }

  get departements(): string[] {
    return ['Tous', ...Array.from(new Set(
      this.demandes.map(demande => demande.departementNom || '-')
    )).sort()];
  }

  get naturesConge() {
    return [
      { value: NatureConge.ANNUEL, label: 'Annuel' },
      { value: NatureConge.MALADIE, label: 'Maladie' },
      { value: NatureConge.MATERNITE, label: 'Maternite' },
      { value: NatureConge.MISE_EN_DISPONIBILITE, label: 'Mise en disponibilite' },
    ];
  }

  loadDemandes(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request$ = this.isModeValidees
      ? this.directeurGeneralDemandeService.getDemandesValidees()
      : this.mode === 'absences-a-valider'
        ? this.directeurGeneralDemandeService.getAbsencesAValider()
      : this.directeurGeneralDemandeService.getDemandesAValider();

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
        console.error('Erreur demandes DG', error);
        this.demandes = [];
        this.demandesFiltrees = [];
        this.errorMessage = this.getErrorMessage(error);
      },
    });
  }

  applyFilters(): void {
    const search = this.searchTerm.trim().toLowerCase();

    const result = this.demandes.filter(demande => {
      const employe = demande.employeNomComplet?.toLowerCase() || '';
      const departement = demande.departementNom || '-';
      const matchSearch = !search || employe.includes(search);
      const matchType = this.selectedType === 'Tous' || demande.typeDemande === this.selectedType;
      const matchDepartement = this.selectedDepartement === 'Tous' || departement === this.selectedDepartement;

      return matchSearch && matchType && matchDepartement;
    });

    this.demandesFiltrees = [...result];
  }

  validerSansModification(demande: DirecteurGeneralDemande): void {
    this.executerAction(
      demande,
      'valider',
      demande.typeDemande === 'ABSENCE'
        ? this.directeurGeneralDemandeService.validerAbsence(demande.id, {
          dateDebutDg: null,
          dateFinDg: null,
        })
        : this.directeurGeneralDemandeService.validerDemande(demande.id, {
        dateDebutDg: null,
        dateFinDg: null,
      }),
      demande.typeDemande === 'ABSENCE'
        ? 'Absence validee par le directeur general.'
        : 'Demande validee par le directeur general.'
    );
  }

  refuserDemande(demande: DirecteurGeneralDemande): void {
    if (!this.hasValidId(demande)) {
      this.errorMessage = 'Impossible de refuser : id de demande manquant';
      return;
    }

    this.executerAction(
      demande,
      'refuser',
      demande.typeDemande === 'ABSENCE'
        ? this.directeurGeneralDemandeService.refuserAbsence(demande.id)
        : this.directeurGeneralDemandeService.refuserDemande(demande.id),
      demande.typeDemande === 'ABSENCE'
        ? 'Absence refusee par le directeur general.'
        : 'Demande refusee par le directeur general.'
    );
  }

  ouvrirModification(demande: DirecteurGeneralDemande): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.demandeEnModification = demande;
    this.modificationForm = {
      dateDebutDg: demande.dateDebutDg || demande.dateDebutResp || demande.dateDebutEmp,
      dateFinDg: demande.dateFinDg || demande.dateFinResp || demande.dateFinEmp,
    };
  }

  passerEnModificationDg(demande: DirecteurGeneralDemande): void {
    if (!this.peutPasserEnModificationDg(demande)) {
      this.ouvrirModification(demande);
      return;
    }

    this.executerAction(
      demande,
      'modifier',
      this.directeurGeneralDemandeService.passerEnModificationDg(demande.id),
      'Demande repassee en modification DG.',
      updatedDemande => {
        this.ouvrirModification(updatedDemande);
      }
    );
  }

  peutPasserEnModificationDg(demande: DirecteurGeneralDemande): boolean {
    return demande.status === 'VALIDE_DG';
  }

  peutRevaliderModificationDg(demande: DirecteurGeneralDemande): boolean {
    return demande.status === 'MODIFICATION_DG';
  }

  validerAvecModification(): void {
    if (!this.demandeEnModification) {
      return;
    }

    const payload: DirecteurGeneralValidationDemandeRequest = {
      dateDebutDg: this.modificationForm.dateDebutDg || null,
      dateFinDg: this.modificationForm.dateFinDg || null,
    };

    this.executerAction(
      this.demandeEnModification,
      'valider',
      this.demandeEnModification.typeDemande === 'ABSENCE'
        ? this.directeurGeneralDemandeService.validerAbsence(this.demandeEnModification.id, payload)
        : this.directeurGeneralDemandeService.validerDemande(this.demandeEnModification.id, payload),
      this.demandeEnModification.typeDemande === 'ABSENCE'
        ? 'Absence validee avec les dates finales DG.'
        : 'Demande validee avec les dates finales DG.',
      () => this.fermerModal()
    );
  }

  fermerModal(): void {
    this.demandeEnModification = null;
    this.modificationForm = {
      dateDebutDg: null,
      dateFinDg: null,
    };
  }

  isActionLoading(demande: DirecteurGeneralDemande, action: ActionDg): boolean {
    return this.actionLoadingId === demande.id && this.actionLoadingType === action;
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
      VALIDE_RESPONSABLE: 'En attente DG',
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

  private hasValidId(demande: DirecteurGeneralDemande | null | undefined): demande is DirecteurGeneralDemande {
    return Number.isFinite(demande?.id) && Number(demande?.id) > 0;
  }

  private executerAction(
    demande: DirecteurGeneralDemande,
    action: ActionDg,
    request$: Observable<DirecteurGeneralDemande>,
    successMessage: string,
    afterSuccess?: (demande: DirecteurGeneralDemande) => void
  ): void {
    this.actionLoadingId = demande.id;
    this.actionLoadingType = action;
    this.errorMessage = '';
    this.successMessage = '';

    request$.pipe(
      finalize(() => {
        this.actionLoadingId = null;
        this.actionLoadingType = null;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: updatedDemande => {
        this.successMessage = successMessage;
        afterSuccess?.(updatedDemande);
        this.loadDemandes();
      },
      error: error => {
        console.error('Erreur action DG', error);
        this.errorMessage = this.getErrorMessage(error);
      },
    });
  }

  private syncModeFromUrl(): void {
    if (this.router.url.includes('/demandes-validees')) {
      this.mode = 'validees';
      return;
    }
    this.mode = this.router.url.includes('/absences-a-valider') ? 'absences-a-valider' : 'a-valider';
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error;
      }
      return error.error?.message || 'Erreur lors du traitement de la demande';
    }
    if (error instanceof Error && error.message.trim()) {
      return error.message;
    }

    return 'Erreur lors du traitement de la demande';
  }
}
