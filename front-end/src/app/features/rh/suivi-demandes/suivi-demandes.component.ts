import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { RhDemandeSuivi, RhDepartement } from '../../../core/models/rh-demande-suivi.model';
import { RhSuiviDemandesService } from '../../../core/services/rh-suivi-demandes.service';

type SuiviMode = 'conges' | 'absences';

@Component({
  selector: 'app-rh-suivi-demandes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './suivi-demandes.component.html',
  styleUrls: ['./suivi-demandes.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RhSuiviDemandesComponent implements OnInit {
  mode: SuiviMode = 'conges';
  demandes: RhDemandeSuivi[] = [];
  departements: RhDepartement[] = [];

  loading = false;
  exporting = false;
  printingId: number | null = null;
  errorMessage = '';
  successMessage = '';

  page = 0;
  size = 4;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;

  search = '';
  annee: number | null = new Date().getFullYear();
  mois: number | null = null;
  departementId: number | null = null;
  readonly moisOptions = [
    { value: 1, label: 'Janvier' },
    { value: 2, label: 'Fevrier' },
    { value: 3, label: 'Mars' },
    { value: 4, label: 'Avril' },
    { value: 5, label: 'Mai' },
    { value: 6, label: 'Juin' },
    { value: 7, label: 'Juillet' },
    { value: 8, label: 'Aout' },
    { value: 9, label: 'Septembre' },
    { value: 10, label: 'Octobre' },
    { value: 11, label: 'Novembre' },
    { value: 12, label: 'Decembre' },
  ];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly rhSuiviService: RhSuiviDemandesService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data['mode'] === 'absences' ? 'absences' : 'conges';
      this.page = 0;
      this.loadDemandes();
      this.loadDepartements();
    });
  }

  get title(): string {
    return this.mode === 'conges' ? 'Conges valides DG' : 'Absences validees DG';
  }

  get subtitle(): string {
    return this.mode === 'conges'
      ? 'Demandes de conge validees par le Directeur General, pretes pour suivi RH.'
      : 'Absences validees par le Directeur General, pretes pour suivi RH.';
  }

  get emptyMessage(): string {
    return this.mode === 'conges' ? 'Aucun conge valide trouve' : 'Aucune absence validee trouvee';
  }

  get isConges(): boolean {
    return this.mode === 'conges';
  }

  loadDemandes(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = this.isConges
      ? this.rhSuiviService.getCongesValides(this.page, this.size, this.annee, this.mois, this.search, this.departementId)
      : this.rhSuiviService.getAbsencesValidees(this.page, this.size, this.annee, this.mois, this.search, this.departementId);

    request.pipe(finalize(() => {
      this.loading = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: response => {
        this.demandes = [...(response.content ?? [])];
        this.page = response.page ?? response.currentPage ?? 0;
        this.size = response.size;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.first = response.first ?? this.page === 0;
        this.last = response.last ?? this.page >= this.totalPages - 1;
      },
      error: error => {
        this.demandes = [];
        this.errorMessage = this.getErrorMessage(error, 'Erreur lors du chargement');
      },
    });
  }

  loadDepartements(): void {
    this.rhSuiviService.getDepartements().subscribe({
      next: departements => {
        this.departements = [...departements];
        this.cdr.markForCheck();
      },
      error: () => {
        this.departements = [];
        this.cdr.markForCheck();
      },
    });
  }

  onFilterChange(): void {
    this.page = 0;
    this.loadDemandes();
  }

  previousPage(): void {
    if (this.first || this.loading) {
      return;
    }
    this.page -= 1;
    this.loadDemandes();
  }

  nextPage(): void {
    if (this.last || this.loading) {
      return;
    }
    this.page += 1;
    this.loadDemandes();
  }

  exporterExcel(): void {
    this.exporting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = this.isConges
      ? this.rhSuiviService.exportCongesExcel(this.annee, this.mois, this.search, this.departementId)
      : this.rhSuiviService.exportAbsencesExcel(this.annee, this.mois, this.search, this.departementId);

    request.pipe(finalize(() => {
      this.exporting = false;
      this.cdr.markForCheck();
    })).subscribe({
      next: blob => {
        this.downloadBlob(blob, this.exportFileName());
        this.successMessage = 'Export Excel telecharge.';
      },
      error: error => {
        this.errorMessage = this.getErrorMessage(error, "Erreur lors de l'export Excel");
      },
    });
  }

  imprimerConge(demande: RhDemandeSuivi): void {
    this.ouvrirImpression(demande);
  }

  imprimerAbsence(demande: RhDemandeSuivi): void {
    this.ouvrirImpression(demande);
  }

  ouvrirImpression(demande: RhDemandeSuivi): void {
    if (!demande?.id) {
      this.errorMessage = 'Id de demande manquant';
      return;
    }
    const url = `/demande-conge/${demande.id}/impression`;
    console.log('Ouverture impression demande id:', demande.id);
    console.log('URL impression:', url);
    window.open(url, '_blank');
  }

  reference(demande: RhDemandeSuivi): string {
    return demande.reference || `DEM-${demande.id}`;
  }

  formatDate(value: string | null): string {
    if (!value) {
      return '-';
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('fr-FR');
  }

  duree(demande: RhDemandeSuivi): number {
    return demande.joursDeduits ?? 0;
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private exportFileName(): string {
    const prefix = this.isConges ? 'conges-valides-dg' : 'absences-valides-dg';
    if (!this.annee) {
      return `${prefix}.xlsx`;
    }
    if (!this.mois) {
      return `${prefix}-${this.annee}.xlsx`;
    }
    return `${prefix}-${this.annee}-${String(this.mois).padStart(2, '0')}.xlsx`;
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
