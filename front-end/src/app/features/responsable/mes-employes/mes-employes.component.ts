import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ResponsableEmploye } from '../../../core/models/responsable-employe.model';
import { ResponsableEmployeService } from '../../../core/services/responsable-employe.service';

@Component({
  selector: 'app-responsable-mes-employes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-employes.component.html',
  styleUrls: ['./mes-employes.component.scss'],
})
export class ResponsableMesEmployesComponent implements OnInit {
  employes: ResponsableEmploye[] = [];
  filteredEmployes: ResponsableEmploye[] = [];
  selectedEmploye?: ResponsableEmploye;

  searchTerm = '';
  selectedRole = 'Tous';
  loading = false;
  errorMessage = '';

  readonly roles = ['Tous', 'EMPLOYE', 'RH'];

  constructor(
    private readonly responsableEmployeService: ResponsableEmployeService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEmployes();
  }

  loadEmployes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.responsableEmployeService.getMesEmployes().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: data => {
        this.employes = [...data];
        this.applyFilters();
        this.cdr.markForCheck();
      },
      error: error => {
        this.errorMessage = this.extractErrorMessage(error);
        this.employes = [];
        this.filteredEmployes = [];
        this.cdr.markForCheck();
      },
    });
  }

  applyFilters(): void {
    const search = this.searchTerm.trim().toLowerCase();
    const result = this.employes.filter(employe => {
      const matchSearch = !search || [
        employe.nom,
        employe.prenom,
        employe.email,
        employe.matricule,
      ].some(value => (value ?? '').toLowerCase().includes(search));

      const matchRole = this.selectedRole === 'Tous' || employe.role === this.selectedRole;
      return matchSearch && matchRole;
    });

    this.filteredEmployes = [...result];
  }

  nomComplet(employe: ResponsableEmploye): string {
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

  voirDetail(employe: ResponsableEmploye): void {
    this.selectedEmploye = { ...employe };
  }

  fermerDetail(): void {
    this.selectedEmploye = undefined;
  }

  private extractErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? 'Erreur lors du chargement des employes';
    }
    return 'Erreur lors du chargement des employes';
  }
}
