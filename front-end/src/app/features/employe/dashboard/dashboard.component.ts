import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DashboardRecentDemande,
  EmployeDashboardResponse,
} from '../../../core/models/dashboard.model';
import { EmployeDashboardService } from '../../../core/services/employe-dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  dashboard: EmployeDashboardResponse | null = null;
  stats: { label: string; value: string; unit: string; color: string; icon: string }[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly dashboardService: EmployeDashboardService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  get recentDemandes(): DashboardRecentDemande[] {
    return this.dashboard?.latestDemandes ?? [];
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';
    this.dashboardService.getDashboard()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: dashboard => {
          if (!dashboard) {
            this.errorMessage = 'Dashboard vide';
            this.dashboard = null;
            this.stats = [];
            this.cdr.detectChanges();
            return;
          }
          this.dashboard = dashboard;
          this.stats = [
            { label: 'Solde conge actuel', value: `${dashboard.soldeActuel ?? 0}`, unit: 'jours', color: 'navy', icon: 'sun' },
            { label: 'Brouillons', value: `${dashboard.brouillonsCount ?? 0}`, unit: 'demandes', color: 'gold', icon: 'clock' },
            { label: 'En attente responsable', value: `${dashboard.demandesEnAttenteResponsable ?? 0}`, unit: 'demandes', color: 'slate', icon: 'calendar' },
            { label: 'Absences ce mois', value: `${dashboard.absencesThisMonth ?? 0}`, unit: 'absence(s)', color: 'teal', icon: 'refresh' },
          ];
          this.cdr.detectChanges();
        },
        error: error => {
          this.errorMessage = error.error?.message || 'Erreur lors du chargement du dashboard';
          this.dashboard = null;
          this.stats = [];
          this.cdr.detectChanges();
        },
      });
  }

  typeLabel(demande: DashboardRecentDemande): string {
    return demande.typeDemande === 'ABSENCE' ? 'Absence' : 'Conge';
  }

  statusLabel(status: string): string {
    return status.replaceAll('_', ' ');
  }

  statusClass(status: string): string {
    if (status.includes('REFUSE')) {
      return 'rejected';
    }
    if (status.includes('VALIDE_DG')) {
      return 'approved';
    }
    return 'pending';
  }
}
