import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { Role } from '../../../core/enums/role.enum';
import { ProfilResponse, ProfilUpdateRequest } from '../../../core/models/profil.model';
import { AuthService } from '../../../core/services/auth.service';
import { ProfilService } from '../../../core/services/profil.service';

@Component({
  selector: 'app-shared-profil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.scss'],
})
export class SharedProfilComponent implements OnInit {
  profil?: ProfilResponse;
  form: ProfilUpdateRequest = {
    nom: '',
    prenom: '',
  };

  role: Role;
  loading = false;
  saving = false;
  editMode = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private readonly profilService: ProfilService,
    private readonly authService: AuthService,
    private readonly route: ActivatedRoute,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.role = this.route.snapshot.data['role'] as Role;
  }

  ngOnInit(): void {
    this.loadProfil();
  }

  get nomComplet(): string {
    if (!this.profil) {
      return '';
    }
    return `${this.profil.prenom} ${this.profil.nom}`.trim();
  }

  get initiales(): string {
    if (!this.profil) {
      return '--';
    }
    return `${this.profil.prenom?.charAt(0) ?? ''}${this.profil.nom?.charAt(0) ?? ''}`.toUpperCase();
  }

  get afficherManager(): boolean {
    return this.profil?.role !== Role.DIRECTEUR_GENERAL;
  }

  get afficherSolde(): boolean {
    return this.profil?.soldeActuel !== null
      && this.profil?.soldeActuel !== undefined
      && this.profil?.soldeTotal !== null
      && this.profil?.soldeTotal !== undefined;
  }

  loadProfil(): void {
    this.loading = true;
    this.errorMessage = '';

    this.profilService.getProfil(this.role).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: data => {
        this.profil = { ...data };
        this.form = {
          nom: data.nom,
          prenom: data.prenom,
        };
        this.cdr.markForCheck();
      },
      error: error => this.handleError(error, 'Erreur lors du chargement du profil'),
    });
  }

  activerEdition(): void {
    if (!this.profil) {
      return;
    }
    this.errorMessage = '';
    this.successMessage = '';
    this.editMode = true;
    this.form = {
      nom: this.profil.nom,
      prenom: this.profil.prenom,
    };
  }

  annulerEdition(): void {
    this.editMode = false;
    this.errorMessage = '';
    if (this.profil) {
      this.form = {
        nom: this.profil.nom,
        prenom: this.profil.prenom,
      };
    }
  }

  enregistrer(): void {
    if (!this.form.nom?.trim()) {
      this.errorMessage = 'Nom obligatoire';
      return;
    }
    if (!this.form.prenom?.trim()) {
      this.errorMessage = 'Prenom obligatoire';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.profilService.updateProfil(this.role, {
      nom: this.form.nom.trim(),
      prenom: this.form.prenom.trim(),
    }).pipe(
      finalize(() => {
        this.saving = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: data => {
        this.profil = { ...data };
        this.form = {
          nom: data.nom,
          prenom: data.prenom,
        };
        this.editMode = false;
        this.successMessage = 'Profil mis a jour avec succes';
        this.authService.updateCurrentUserProfileFromEmploye(data.nom, data.prenom);
        this.cdr.markForCheck();
      },
      error: error => this.handleError(error, 'Erreur lors de la mise a jour du profil'),
    });
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
}
