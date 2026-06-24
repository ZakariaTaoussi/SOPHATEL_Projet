import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  DemandeCongeImpression,
  NatureConge,
} from '../../../core/models/demande-conge.model';
import { DemandeCongeImpressionService } from '../../../core/services/demande-conge-impression.service';

@Component({
  selector: 'app-demande-conge-print',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './demande-conge-print.component.html',
  styleUrls: ['./demande-conge-print.component.css'],
})
export class DemandeCongePrintComponent implements OnInit {
  demande: DemandeCongeImpression | null = null;
  loading = true;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly impressionService: DemandeCongeImpressionService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id) || id <= 0) {
      this.errorMessage = 'Demande introuvable.';
      this.loading = false;
      return;
    }

    console.log('ID impression:', id);
    this.impressionService.getDemandePourImpression(id).subscribe({
      next: data => {
        console.log('Données impression:', data);
        this.demande = data;
        this.loading = false;
        this.changeDetectorRef.detectChanges();

        setTimeout(() => {
          window.print();
        }, 800);
      },
      error: error => {
        console.error('Erreur impression:', error);
        this.loading = false;
        this.errorMessage = this.getErrorMessage(error) || 'Impossible de charger la demande.';
      },
    });
  }

  print(): void {
    window.print();
  }

  isNature(nature: NatureConge | string): boolean {
    return this.demande?.natureConge === nature;
  }

  formatStatus(status?: string | null): string {
    if (!status) {
      return '-';
    }
    return status;
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error;
      }
      return error.error?.message || 'Impossible de charger la demande pour impression.';
    }
    if (error instanceof Error && error.message.trim()) {
      return error.message;
    }
    return 'Impossible de charger la demande pour impression.';
  }
}
