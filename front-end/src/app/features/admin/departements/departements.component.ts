import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Departement } from '../../../core/models/departement.model';
import { DepartementService } from '../../../core/services/departement.service';

@Component({
  selector: 'app-admin-departements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './departements.component.html',
  styleUrls: ['./departements.component.scss'],
})
export class AdminDepartementsComponent implements OnInit {
  editingId?: number;
  form = this.emptyForm();
  departements: Departement[] = [];
  errorMessage = '';
  isLoading = false;

  constructor(
    private readonly departementService: DepartementService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerDepartements();
  }

  chargerDepartements(): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    this.departementService.consulterDepartements().subscribe({
      next: departements => {
        this.departements = departements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  enregistrer(): void {
    if (!this.form.nom.trim()) return;

    const request = { nom: this.form.nom.trim() };
    const action = this.editingId
      ? this.departementService.modifierDepartement(this.editingId, request)
      : this.departementService.creerDepartement(request);

    action.subscribe({
      next: savedDepartement => {
        this.mettreAJourListeLocale(savedDepartement);
        this.reinitialiser();
        this.chargerDepartements();
      },
      error: error => this.handleError(error),
    });
  }

  modifier(departement: Departement): void {
    this.editingId = departement.id;
    this.form = { nom: departement.nom };
  }

  supprimer(departement: Departement): void {
    this.departementService.supprimerDepartement(departement.id).subscribe({
      next: () => {
        if (this.editingId === departement.id) this.reinitialiser();
        this.departements = this.departements.filter(item => item.id !== departement.id);
        this.cdr.detectChanges();
        this.chargerDepartements();
      },
      error: error => this.handleError(error),
    });
  }

  reinitialiser(): void {
    this.editingId = undefined;
    this.form = this.emptyForm();
    this.errorMessage = '';
  }

  private handleError(error: unknown): void {
    console.error('Erreur HTTP admin departements', error);
    this.isLoading = false;
    this.errorMessage = error instanceof HttpErrorResponse
      ? error.error?.message ?? 'Une erreur est survenue.'
      : 'Une erreur est survenue.';
    this.cdr.detectChanges();
  }

  private emptyForm(): { nom: string } {
    return { nom: '' };
  }

  private mettreAJourListeLocale(departement: Departement): void {
    this.departements = this.editingId
      ? this.departements.map(item => item.id === departement.id ? departement : item)
      : [...this.departements, departement];
    this.isLoading = false;
    this.cdr.detectChanges();
  }
}
