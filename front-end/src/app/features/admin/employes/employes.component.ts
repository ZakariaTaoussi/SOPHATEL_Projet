import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Role } from '../../../core/enums/role.enum';
import { Departement } from '../../../core/models/departement.model';
import { CreateEmployeRequest, Employe, StatutEmploye } from '../../../core/models/employe.model';
import { DepartementService } from '../../../core/services/departement.service';
import { EmployeAdminService } from '../../../core/services/employe-admin.service';

@Component({
  selector: 'app-admin-employes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employes.component.html',
  styleUrls: ['./employes.component.scss'],
})
export class AdminEmployesComponent implements OnInit {
  searchTerm = '';
  editingId?: number;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  readonly pageSize = 3;

  departements: Departement[] = [];
  roles = Object.values(Role);
  statuts: StatutEmploye[] = ['ACTIF', 'INACTIF'];
  employes: Employe[] = [];
  form: CreateEmployeRequest = this.emptyForm();
  errorMessage = '';
  isLoading = false;

  constructor(
    private readonly employeAdminService: EmployeAdminService,
    private readonly departementService: DepartementService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerDepartements();
    this.chargerEmployes();
  }

  chargerDepartements(): void {
    this.departementService.consulterDepartements().subscribe({
      next: departements => {
        this.departements = departements;
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  chargerEmployes(page = this.currentPage): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    this.employeAdminService.consulterEmployes(page, this.pageSize, this.searchTerm).subscribe({
      next: response => {
        this.employes = response.content;
        this.currentPage = response.currentPage;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: error => this.handleError(error),
    });
  }

  rechercher(): void {
    this.chargerEmployes(0);
  }

  enregistrer(): void {
    if (!this.form.matricule.trim() || !this.form.nom.trim() || !this.form.prenom.trim() || !this.form.email.trim()) {
      return;
    }
    if (!this.editingId && !this.form.password.trim()) {
      this.errorMessage = 'Le mot de passe est obligatoire.';
      return;
    }

    const request = { ...this.form };
    const action = this.editingId
      ? this.employeAdminService.modifierEmploye(this.editingId, request)
      : this.employeAdminService.creerEmploye(request);

    action.subscribe({
      next: savedEmploye => {
        const pageToReload = this.editingId ? this.currentPage : 0;
        this.mettreAJourListeLocale(savedEmploye);
        this.reinitialiser();
        this.chargerEmployes(pageToReload);
      },
      error: error => this.handleError(error),
    });
  }

  modifier(employe: Employe): void {
    this.editingId = employe.id;
    this.form = {
      matricule: employe.matricule,
      nom: employe.nom,
      prenom: employe.prenom,
      email: employe.email,
      password: '',
      role: employe.role,
      departementId: employe.departementId,
      statut: employe.statut,
    };
  }

  supprimer(employe: Employe): void {
    this.employeAdminService.supprimerEmploye(employe.id).subscribe({
      next: () => {
        if (this.editingId === employe.id) this.reinitialiser();
        const nextPage = this.employes.length === 1 && this.currentPage > 0 ? this.currentPage - 1 : this.currentPage;
        this.employes = this.employes.filter(item => item.id !== employe.id);
        this.totalElements = Math.max(this.totalElements - 1, 0);
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);
        this.cdr.detectChanges();
        this.chargerEmployes(nextPage);
      },
      error: error => this.handleError(error),
    });
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.chargerEmployes(this.currentPage - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage + 1 < this.totalPages) {
      this.chargerEmployes(this.currentPage + 1);
    }
  }

  reinitialiser(): void {
    this.editingId = undefined;
    this.form = this.emptyForm();
    this.errorMessage = '';
  }

  roleLabel(role: Role): string {
    return role.replace('_', ' ');
  }

  private handleError(error: unknown): void {
    console.error('Erreur HTTP admin employes', error);
    this.isLoading = false;
    this.errorMessage = error instanceof HttpErrorResponse
      ? error.error?.message ?? 'Une erreur est survenue.'
      : 'Une erreur est survenue.';
    this.cdr.detectChanges();
  }

  private emptyForm(): CreateEmployeRequest {
    return {
      matricule: '',
      nom: '',
      prenom: '',
      email: '',
      password: '',
      role: Role.EMPLOYE,
      departementId: null,
      statut: 'ACTIF',
    };
  }

  private mettreAJourListeLocale(employe: Employe): void {
    if (this.editingId) {
      this.employes = this.employes.map(item => item.id === employe.id ? employe : item);
    } else {
      this.currentPage = 0;
      this.totalElements += 1;
      this.totalPages = Math.ceil(this.totalElements / this.pageSize);
      this.employes = [employe, ...this.employes].slice(0, this.pageSize);
    }

    this.isLoading = false;
    this.cdr.detectChanges();
  }
}
