import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { finalize, Subscription } from 'rxjs';
import { DemandeConge } from '../../../core/models/demande-conge.model';
import { DemandeCongeService } from '../../../core/services/demande-conge.service';

@Component({
  selector: 'app-responsable-validation-demandes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './validation-demandes.component.html',
  styleUrls: ['./validation-demandes.component.scss'],
})
export class ResponsableValidationDemandesComponent implements OnInit, OnDestroy {
  demandes: DemandeConge[] = [];
  loading = false;
  errorMessage = '';
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly demandeCongeService: DemandeCongeService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.demandeCongeService.demandesAValider$.subscribe(demandes => {
        this.demandes = demandes;
        this.cdr.detectChanges();
      })
    );
    this.loadDemandesAValider();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  loadDemandesAValider(): void {
    this.loading = true;
    this.errorMessage = '';

    this.demandeCongeService.getDemandesAValiderResponsable().pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: demandes => {
        this.loading = false;
        this.demandes = demandes;
        this.cdr.detectChanges();
      },
      error: error => {
        this.handleError(error);
        this.cdr.detectChanges();
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

  private handleError(error: unknown): void {
    console.error('Erreur demandes responsable', error);
    if (error instanceof HttpErrorResponse) {
      this.errorMessage = typeof error.error === 'string'
        ? error.error
        : error.error?.message ?? 'Une erreur est survenue.';
      return;
    }

    this.errorMessage = 'Une erreur est survenue.';
  }
}
