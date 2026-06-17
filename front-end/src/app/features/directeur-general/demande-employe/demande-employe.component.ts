import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ResponsableDemande } from '../../../core/models/demande-conge.model';
import { DirecteurGeneralDemandeService } from '../../../core/services/directeur-general-demande.service';

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
  demandes: ResponsableDemande[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly directeurGeneralDemandeService: DirecteurGeneralDemandeService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDemandes();
  }

  get types(): string[] {
    return ['Tous', 'CONGE', 'ABSENCE'];
  }

  get departements(): string[] {
    return ['Tous', ...Array.from(new Set(
      this.demandes.map(demande => demande.departementNom || '-')
    )).sort()];
  }

  get demandesFiltrees(): ResponsableDemande[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.demandes.filter(demande => {
      const employe = demande.employeNomComplet?.toLowerCase() || '';
      const departement = demande.departementNom || '-';
      const matchSearch = !search || employe.includes(search);
      const matchType = this.selectedType === 'Tous' || demande.typeDemande === this.selectedType;
      const matchDepartement = this.selectedDepartement === 'Tous' || departement === this.selectedDepartement;
      return matchSearch && matchType && matchDepartement;
    });
  }

  loadDemandes(): void {
    this.loading = true;
    this.errorMessage = '';

    this.directeurGeneralDemandeService.getDemandesAValider().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: demandes => {
        this.demandes = [...demandes];
      },
      error: error => {
        console.error('Erreur demandes DG', error);
        this.demandes = [];
        this.errorMessage = this.getErrorMessage(error);
      },
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

  getDateDebut(demande: ResponsableDemande): string {
    return demande.dateDebutResp || demande.dateDebutEmp;
  }

  getDateFin(demande: ResponsableDemande): string {
    return demande.dateFinResp || demande.dateFinEmp;
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error;
      }
      return error.error?.message || 'Erreur lors du chargement des demandes';
    }

    return 'Erreur lors du chargement des demandes';
  }
}
