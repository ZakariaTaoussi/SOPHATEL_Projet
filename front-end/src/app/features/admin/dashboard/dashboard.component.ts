import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AdminDashboardResponse } from '../../../core/models/dashboard.model';
import { AdminDashboardService } from '../../../core/services/admin-dashboard.service';

interface StatCard {
  label: string;
  value: string;
  detail: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  dashboard: AdminDashboardResponse | null = null;
  stats: StatCard[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private readonly dashboardService: AdminDashboardService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
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
            {
              label: 'Total employes',
              value: `${dashboard.totalEmployees ?? 0}`,
              detail: `${dashboard.totalEmployesRole ?? 0} employes, ${dashboard.totalResponsables ?? 0} responsables, ${dashboard.totalRh ?? 0} RH, ${dashboard.totalDirecteursGeneraux ?? 0} DG`,
            },
            { label: 'Administrateurs', value: `${dashboard.totalAdmins ?? 0}`, detail: 'Comptes systeme actifs' },
            { label: 'Departements', value: `${dashboard.totalDepartments ?? 0}`, detail: 'Structures configurees' },
            {
              label: `Jours feries ${new Date().getFullYear()}`,
              value: `${dashboard.holidaysThisYear ?? 0}`,
              detail: dashboard.currentYearAgendaExists ? 'Agenda annuel cree' : 'Agenda annuel non cree',
            },
          ];
          this.cdr.detectChanges();
        },
        error: error => {
          this.errorMessage = error.error?.message || 'Erreur lors du chargement du dashboard admin';
          this.dashboard = null;
          this.stats = [];
          this.cdr.detectChanges();
        },
      });
  }
}
