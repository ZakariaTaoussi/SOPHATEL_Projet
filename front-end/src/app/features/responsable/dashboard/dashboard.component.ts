import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DashboardRecentDemande,
  ResponsableDashboardResponse,
} from '../../../core/models/dashboard.model';
import { ResponsableDashboardService } from '../../../core/services/responsable-dashboard.service';

@Component({
  selector: 'app-responsable-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class ResponsableDashboardComponent implements OnInit {
  dashboard: ResponsableDashboardResponse | null = null;
  stats: { label: string; value: string; unit: string; color: string; icon: string }[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly dashboardService: ResponsableDashboardService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  get latestTeamRequests(): DashboardRecentDemande[] {
    return this.dashboard?.latestTeamRequests ?? [];
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
            { label: 'Employes departement', value: `${dashboard.teamMembersCount ?? 0}`, unit: 'employe(s)', color: 'navy', icon: 'users' },
            { label: 'Conges a valider', value: `${dashboard.pendingTeamConges ?? 0}`, unit: 'conge(s)', color: 'gold', icon: 'clock' },
            { label: 'Absences a valider', value: `${dashboard.pendingTeamAbsences ?? 0}`, unit: 'absence(s)', color: 'slate', icon: 'calendar' },
            { label: 'Validees DG ce mois', value: `${dashboard.validatedByDgForTeamThisMonth ?? 0}`, unit: 'demande(s)', color: 'teal', icon: 'check' },
          ];
          this.cdr.detectChanges();
        },
        error: error => {
          this.errorMessage = error.error?.message || 'Erreur lors du chargement du dashboard responsable';
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
    if (status.includes('VALIDE_DG') || status.includes('VALIDE_RESPONSABLE')) {
      return 'approved';
    }
    return 'pending';
  }
}
