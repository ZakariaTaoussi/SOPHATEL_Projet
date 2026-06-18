import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DemandeCongeCreateRequest,
  SoldeConge,
  TypeDemande,
} from '../../../core/models/demande-conge.model';
import {
  SelfDemandeCongeService,
  SelfDemandeScope,
} from '../../../core/services/self-demande-conge.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-self-nouvelle-demande',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nouvelle-demande.component.html',
  styleUrls: ['../../employe/nouvelle-demande/nouvelle-demande.component.scss'],
})
export class SelfNouvelleDemandeComponent implements OnInit {
  typesDemande: TypeDemande[] = ['CONGE', 'ABSENCE'];

  form: DemandeCongeCreateRequest = {
    typeDemande: 'CONGE',
    dateDebutEmp: '',
    dateFinEmp: '',
  };

  solde?: SoldeConge;
  joursApercu: number | null = null;
  demandeBrouillonId?: number;
  loadingSolde = false;
  savingDraft = false;
  submitting = false;
  errorMessage = '';

  private readonly scope: SelfDemandeScope;
  private readonly baseRoute: string;

  constructor(
    private readonly demandeService: SelfDemandeCongeService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.scope = this.route.snapshot.data['scope'] as SelfDemandeScope;
    this.baseRoute = this.route.snapshot.data['baseRoute'] as string;
  }

  ngOnInit(): void {
    this.loadSolde();
  }

  get periode(): string {
    if (!this.form.dateDebutEmp || !this.form.dateFinEmp) {
      return '-';
    }

    return `${this.formatDate(this.form.dateDebutEmp)} - ${this.formatDate(this.form.dateFinEmp)}`;
  }

  get soldeApres(): string {
    if (this.form.typeDemande !== 'CONGE' || this.joursApercu === null || !this.solde) {
      return '-';
    }

    return `${Math.max(this.solde.soldeActuel - this.joursApercu, 0)} / ${this.solde.soldeTotal} j`;
  }

  loadSolde(): void {
    this.loadingSolde = true;
    this.demandeService.getSoldeConge(this.scope).pipe(
      finalize(() => {
        this.loadingSolde = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: solde => {
        this.solde = solde;
        this.cdr.markForCheck();
      },
      error: error => this.handleError(error),
    });
  }

  onFormChange(): void {
    this.errorMessage = '';
    this.demandeBrouillonId = undefined;
    this.calculerJours();
  }

  calculerJours(): void {
    if (this.form.typeDemande !== 'CONGE' || !this.form.dateDebutEmp || !this.form.dateFinEmp) {
      this.joursApercu = null;
      return;
    }

    this.demandeService.calculerJours(this.scope, this.form.dateDebutEmp, this.form.dateFinEmp).subscribe({
      next: response => {
        this.joursApercu = response.jours;
        this.cdr.markForCheck();
      },
      error: error => {
        this.joursApercu = null;
        this.handleError(error);
      },
    });
  }

  enregistrerBrouillon(): void {
    if (!this.isFormValid()) {
      return;
    }

    this.savingDraft = true;
    this.errorMessage = '';

    this.demandeService.creerDemande(this.scope, this.form).pipe(
      finalize(() => {
        this.savingDraft = false;
        this.cdr.markForCheck();
      })
    ).subscribe({
      next: demande => {
        this.demandeBrouillonId = demande.id;
        this.notificationService.add(`Demande DEM-${demande.id} enregistree en brouillon.`, 'success');
        this.loadSolde();
      },
      error: error => this.handleError(error),
    });
  }

  soumettreDemande(): void {
    if (!this.isFormValid()) {
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const submitDraft = (id: number) => {
      this.demandeService.submitDemande(this.scope, id).pipe(
        finalize(() => {
          this.submitting = false;
          this.cdr.markForCheck();
        })
      ).subscribe({
        next: demande => {
          this.notificationService.add(`Demande DEM-${demande.id} soumise.`, 'success');
          this.loadSolde();
          this.router.navigate([this.baseRoute, 'mes-demandes']);
        },
        error: error => this.handleError(error),
      });
    };

    if (this.demandeBrouillonId) {
      submitDraft(this.demandeBrouillonId);
      return;
    }

    this.demandeService.creerDemande(this.scope, this.form).subscribe({
      next: demande => submitDraft(demande.id),
      error: error => {
        this.submitting = false;
        this.handleError(error);
        this.cdr.markForCheck();
      },
    });
  }

  private isFormValid(): boolean {
    if (!this.form.dateDebutEmp || !this.form.dateFinEmp || !this.form.typeDemande) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires.';
      return false;
    }

    if (this.form.dateDebutEmp > this.form.dateFinEmp) {
      this.errorMessage = 'Dates invalides';
      return false;
    }

    return true;
  }

  private handleError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      this.errorMessage = typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? 'Une erreur est survenue.';
    } else if (error instanceof Error) {
      this.errorMessage = error.message;
    } else {
      this.errorMessage = 'Une erreur est survenue.';
    }
    this.cdr.markForCheck();
  }

  private formatDate(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(new Date(value));
  }
}
