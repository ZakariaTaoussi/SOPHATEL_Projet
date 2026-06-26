import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DemandeConge,
  StatusDemande,
} from '../../../core/models/demande-conge.model';
import {
  SelfDemandeCongeService,
  SelfDemandeScope,
} from '../../../core/services/self-demande-conge.service';

@Component({
  selector: 'app-mes-absences',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-absences.component.html',
  styleUrls: ['./mes-absences.component.scss'],
})
export class MesAbsencesComponent implements OnInit {
  absences: DemandeConge[] = [];
  selectedYear = new Date().getFullYear();
  selectedMonth = 0;
  currentPage = 1;
  readonly pageSize = 5;
  loading = false;
  errorMessage = '';
  readonly months = [
    { value: 0, label: 'Tous les mois' },
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
    private readonly demandeService: SelfDemandeCongeService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAbsences();
  }

  get years(): number[] {
    const values = new Set<number>([new Date().getFullYear(), this.selectedYear]);
    this.absences.forEach(absence => values.add(new Date(absence.dateDebutEmp).getFullYear()));
    return Array.from(values).sort((a, b) => b - a);
  }

  get filteredAbsences(): DemandeConge[] {
    return this.absences.filter(absence => {
      if (absence.typeDemande !== 'ABSENCE') {
        return false;
      }
      const dateDebut = new Date(absence.dateDebutEmp);
      const matchYear = dateDebut.getFullYear() === Number(this.selectedYear);
      const matchMonth = Number(this.selectedMonth) === 0
        || dateDebut.getMonth() + 1 === Number(this.selectedMonth);
      return matchYear && matchMonth;
    });
  }

  get pagedAbsences(): DemandeConge[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredAbsences.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredAbsences.length / this.pageSize));
  }

  loadAbsences(): void {
    this.loading = true;
    this.errorMessage = '';
    const scope = this.scope;

    this.demandeService.getMesAbsences(scope).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: absences => {
        this.absences = absences.filter(absence => absence.typeDemande === 'ABSENCE');
        this.ensureSelectedYearExists();
        this.ensureValidPage();
        this.cdr.markForCheck();
      },
      error: error => {
        this.absences = [];
        this.errorMessage = this.getErrorMessage(error, 'Erreur lors du chargement des absences');
      },
    });
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.ensureValidPage();
    this.cdr.markForCheck();
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

  declarerAbsence(): void {
    this.router.navigate([this.baseRoute, 'nouvelle-demande']);
  }

  dureeAbsence(absence: DemandeConge): number {
    return absence.joursDeduits || 0;
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
    return labels[status] ?? status;
  }

  private get scope(): SelfDemandeScope {
    if (this.router.url.startsWith('/rh')) {
      return 'rh';
    }
    if (this.router.url.startsWith('/responsable')) {
      return 'responsable';
    }
    return 'employe';
  }

  private get baseRoute(): string {
    if (this.scope === 'rh') {
      return '/rh';
    }
    if (this.scope === 'responsable') {
      return '/responsable';
    }
    return '/employe';
  }

  private ensureSelectedYearExists(): void {
    if (!this.years.includes(Number(this.selectedYear))) {
      this.selectedYear = this.years[0] ?? new Date().getFullYear();
    }
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
