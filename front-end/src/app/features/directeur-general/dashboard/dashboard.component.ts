import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DashboardRecentDemande,
  DirecteurGeneralDashboardResponse,
} from '../../../core/models/dashboard.model';
import { DirecteurGeneralDashboardService } from '../../../core/services/directeur-general-dashboard.service';

@Component({
  selector: 'app-directeur-general-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DirecteurGeneralDashboardComponent implements OnInit {
  dashboard: DirecteurGeneralDashboardResponse | null = null;
  stats: { label: string; value: string; unit: string; color: string; icon: string }[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly dashboardService: DirecteurGeneralDashboardService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  get pendingRequests(): DashboardRecentDemande[] {
    return this.dashboard?.recentResponsableValidatedRequests ?? [];
  }

  get processedRequests(): DashboardRecentDemande[] {
    return this.dashboard?.recentDgProcessedRequests ?? [];
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
            { label: 'Conges a valider', value: `${dashboard.pendingCongesForDg ?? 0}`, unit: 'conge(s)', color: 'navy', icon: 'clock' },
            { label: 'Absences a valider', value: `${dashboard.pendingAbsencesForDg ?? 0}`, unit: 'absence(s)', color: 'gold', icon: 'calendar' },
            { label: 'Validees ce mois', value: `${dashboard.validatedByDgThisMonth ?? 0}`, unit: 'demande(s)', color: 'slate', icon: 'check' },
            { label: 'Total employes', value: `${dashboard.totalEmployeesExceptAdmins ?? 0}`, unit: 'hors admin', color: 'teal', icon: 'users' },
          ];
          this.cdr.detectChanges();
        },
        error: error => {
          this.errorMessage = error.error?.message || 'Erreur lors du chargement du dashboard DG';
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
    if (status.includes('VALIDE')) {
      return 'approved';
    }
    return 'pending';
  }
}
