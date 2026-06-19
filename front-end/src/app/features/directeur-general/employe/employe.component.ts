import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, finalize, takeUntil } from 'rxjs';
import { DirecteurGeneralEmploye } from '../../../core/models/directeur-general-employe.model';
import { DirecteurGeneralEmployeService } from '../../../core/services/directeur-general-employe.service';

@Component({
  selector: 'app-directeur-general-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employe.component.html',
  styleUrls: ['./employe.component.scss'],
})
export class DirecteurGeneralEmployeComponent implements OnInit, OnDestroy {
  employes: DirecteurGeneralEmploye[] = [];

  searchTerm = '';
  selectedRole = '';
  departementId = '';
  page = 0;
  size = 4;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;
  loading = false;
  errorMessage = '';

  readonly roles = [
    { label: 'Tous', value: '' },
    { label: 'Employe', value: 'EMPLOYE' },
    { label: 'RH', value: 'RH' },
    { label: 'Responsable', value: 'RESPONSABLE' },
  ];

  private readonly searchChanged$ = new Subject<void>();
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly directeurGeneralEmployeService: DirecteurGeneralEmployeService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.searchChanged$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => {
        this.page = 0;
        this.loadEmployes();
      });

    this.loadEmployes();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadEmployes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.directeurGeneralEmployeService.getEmployes(
      this.page,
      this.size,
      this.searchTerm,
      this.selectedRole,
      this.getDepartementId()
    ).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: response => {
        this.employes = [...response.content];
        this.page = response.page ?? response.currentPage ?? 0;
        this.size = response.size;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.first = response.first ?? this.page === 0;
        this.last = response.last ?? this.page >= this.totalPages - 1;
        this.cdr.markForCheck();
      },
      error: error => {
        this.errorMessage = this.extractErrorMessage(error);
        this.employes = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.first = true;
        this.last = true;
        this.cdr.markForCheck();
      },
    });
  }

  onSearchChange(): void {
    this.searchChanged$.next();
  }

  onFilterChange(): void {
    this.page = 0;
    this.loadEmployes();
  }

  previousPage(): void {
    if (this.first) {
      return;
    }
    this.page -= 1;
    this.loadEmployes();
  }

  nextPage(): void {
    if (this.last) {
      return;
    }
    this.page += 1;
    this.loadEmployes();
  }

  nomComplet(employe: DirecteurGeneralEmploye): string {
    const fullName = `${employe.prenom ?? ''} ${employe.nom ?? ''}`.trim();
    return fullName || 'Non defini';
  }

  formatDate(value?: string | null): string {
    if (!value) {
      return 'Non defini';
    }
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(value));
  }

  formatNumber(value?: number | null): string {
    return value === null || value === undefined ? 'Non defini' : `${value} j`;
  }

  private extractErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? 'Erreur lors du chargement des employes';
    }
    return 'Erreur lors du chargement des employes';
  }

  private getDepartementId(): number | null {
    if (!this.departementId.trim()) {
      return null;
    }
    const value = Number(this.departementId);
    return Number.isFinite(value) && value > 0 ? value : null;
  }
}
