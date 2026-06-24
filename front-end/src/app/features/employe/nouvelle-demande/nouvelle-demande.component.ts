import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, Subscription } from 'rxjs';
import { DemandeCongeService } from '../../../core/services/demande-conge.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NatureConge, SoldeConge, TypeDemande } from '../../../core/models/demande-conge.model';

@Component({
  selector: 'app-nouvelle-demande',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nouvelle-demande.component.html',
  styleUrls: ['./nouvelle-demande.component.scss'],
})
export class NouvellDemandeComponent implements OnInit, OnDestroy {
  typesDemande: TypeDemande[] = ['CONGE', 'ABSENCE'];
  naturesConge = [
    { value: NatureConge.ANNUEL, label: 'Annuel' },
    { value: NatureConge.MALADIE, label: 'Maladie' },
    { value: NatureConge.MATERNITE, label: 'Maternite' },
    { value: NatureConge.MISE_EN_DISPONIBILITE, label: 'Mise en disponibilite' },
  ];

  form = {
    typeDemande: 'CONGE' as TypeDemande,
    natureConge: null as NatureConge | null,
    dateDebutEmp: '',
    dateFinEmp: '',
  };

  solde?: SoldeConge;
  joursApercu: number | null = null;
  demandeBrouillonId?: number;
  loading = false;
  savingDraft = false;
  submitting = false;
  errorMessage = '';
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly demandeCongeService: DemandeCongeService,
    private readonly notificationService: NotificationService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.demandeCongeService.soldeConge$.subscribe(solde => {
        if (solde) {
          this.solde = solde;
          this.cdr.detectChanges();
        }
      })
    );
    this.loadSolde();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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
    this.demandeCongeService.getSoldeConge().subscribe({
      next: solde => {
        this.solde = solde;
        this.cdr.detectChanges();
      },
      error: error => {
        this.handleError(error);
        this.cdr.detectChanges();
      },
    });
  }

  onFormChange(): void {
    this.errorMessage = '';
    this.demandeBrouillonId = undefined;
    if (this.form.typeDemande === 'ABSENCE') {
      this.form.natureConge = null;
    }
    this.calculerJours();
    this.cdr.detectChanges();
  }

  calculerJours(): void {
    if (this.form.typeDemande !== 'CONGE' || !this.form.dateDebutEmp || !this.form.dateFinEmp) {
      this.joursApercu = null;
      return;
    }

    this.demandeCongeService.calculerJours(this.form.dateDebutEmp, this.form.dateFinEmp).subscribe({
      next: response => {
        this.joursApercu = response.jours;
        this.cdr.detectChanges();
      },
      error: error => {
        this.joursApercu = null;
        this.handleError(error);
        this.cdr.detectChanges();
      },
    });
  }

  enregistrerBrouillon(): void {
    if (!this.isFormValid()) {
      return;
    }

    this.savingDraft = true;
    this.errorMessage = '';

    this.demandeCongeService.creerDemande(this.buildPayload(false)).pipe(
      finalize(() => {
        this.savingDraft = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: demande => {
        this.savingDraft = false;
        this.demandeBrouillonId = demande.id;
        this.demandeCongeService.refreshMesDemandes();
        this.notificationService.add(`Demande DEM-${demande.id} enregistree en brouillon.`, 'success');
        this.cdr.detectChanges();
      },
      error: error => {
        this.handleError(error);
        this.cdr.detectChanges();
      },
    });
  }

  soumettreDemande(): void {
    if (!this.isFormValid()) {
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const submitDraft = (id: number) => {
      this.demandeCongeService.submitDemande(id).pipe(
        finalize(() => {
          this.submitting = false;
          this.cdr.detectChanges();
        })
      ).subscribe({
        next: demande => {
          this.submitting = false;
          this.notificationService.add(`Demande DEM-${demande.id} soumise.`, 'success');
          this.cdr.detectChanges();
          this.router.navigate([demande.typeDemande === 'ABSENCE' ? '/employe/mes-absences' : '/employe/mes-demandes']);
        },
        error: error => {
          this.handleError(error);
          this.cdr.detectChanges();
        },
      });
    };

    if (this.demandeBrouillonId) {
      submitDraft(this.demandeBrouillonId);
      return;
    }

    this.demandeCongeService.creerDemande(this.buildPayload(true)).pipe(
      finalize(() => {
        this.submitting = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: demande => {
        this.notificationService.add(`Demande DEM-${demande.id} soumise.`, 'success');
        this.router.navigate([demande.typeDemande === 'ABSENCE' ? '/employe/mes-absences' : '/employe/mes-demandes']);
      },
      error: error => {
        this.handleError(error);
      },
    });
  }

  private buildPayload(soumettre: boolean) {
    return {
      dateDebutEmp: this.form.dateDebutEmp,
      dateFinEmp: this.form.dateFinEmp,
      typeDemande: this.form.typeDemande,
      natureConge: this.form.typeDemande === 'CONGE' ? this.form.natureConge : null,
      soumettre,
    };
  }

  private isFormValid(): boolean {
    if (!this.form.dateDebutEmp || !this.form.dateFinEmp || !this.form.typeDemande) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires.';
      return false;
    }

    if (this.form.dateDebutEmp > this.form.dateFinEmp) {
      this.errorMessage = 'La date de debut ne peut pas etre apres la date de fin.';
      return false;
    }

    if (this.form.typeDemande === 'CONGE' && !this.form.natureConge) {
      this.errorMessage = 'Veuillez choisir la nature du conge.';
      return false;
    }

    return true;
  }

  private handleError(error: unknown): void {
    console.error('Erreur demande conge', error);
    if (error instanceof HttpErrorResponse) {
      this.errorMessage = typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? 'Une erreur est survenue.';
      return;
    }

    this.errorMessage = 'Une erreur est survenue.';
  }

  private formatDate(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(new Date(value));
  }
}
