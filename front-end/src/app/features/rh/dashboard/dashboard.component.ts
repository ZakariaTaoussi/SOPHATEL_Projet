import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { DashboardRecentDemande, RhDashboardResponse } from '../../../core/models/dashboard.model';
import { RhDashboardService } from '../../../core/services/rh-dashboard.service';

@Component({
  selector: 'app-rh-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class RhDashboardComponent implements OnInit {
  dashboard: RhDashboardResponse | null = null;
  stats: { label: string; value: string; unit: string; color: string; icon: string }[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly dashboardService: RhDashboardService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  get recentConges(): DashboardRecentDemande[] {
    return this.dashboard?.recentCongesValidesDg ?? [];
  }

  get recentAbsences(): DashboardRecentDemande[] {
    return this.dashboard?.recentAbsencesValideesDg ?? [];
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
            { label: 'Conges valides DG ce mois', value: `${dashboard.congesValidesDgThisMonth ?? 0}`, unit: 'conge(s)', color: 'navy', icon: 'file' },
            { label: 'Absences validees DG ce mois', value: `${dashboard.absencesValideesDgThisMonth ?? 0}`, unit: 'absence(s)', color: 'gold', icon: 'calendar' },
            { label: 'Jours conge valides', value: `${dashboard.totalJoursCongeValidesThisMonth ?? 0}`, unit: 'jours', color: 'slate', icon: 'sun' },
            { label: 'Jours absence paie', value: `${dashboard.totalJoursAbsencePaieThisMonth ?? 0}`, unit: 'jours', color: 'teal', icon: 'refresh' },
          ];
          this.cdr.detectChanges();
        },
        error: error => {
          this.errorMessage = error.error?.message || 'Erreur lors du chargement du dashboard RH';
          this.dashboard = null;
          this.stats = [];
          this.cdr.detectChanges();
        },
      });
  }

  statusLabel(status: string): string {
    return status.replaceAll('_', ' ');
  }
}
