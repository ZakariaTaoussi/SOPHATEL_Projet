import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Role } from '../../core/enums/role.enum';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-directeur-general-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './directeur-general-layout.component.html',
  styleUrls: ['./directeur-general-layout.component.scss'],
})
export class DirecteurGeneralLayoutComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  isSidebarCollapsed = signal(false);

  get currentUser() {
    const user = this.authService.currentUser();
    return {
      nom: this.authService.getDisplayName(user),
      role: user?.role ?? Role.DIRECTEUR_GENERAL,
      avatar: this.authService.getInitials(user) || 'DG',
      departement: user?.departementNom ?? 'Non defini',
    };
  }

  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/directeur-general/dashboard', icon: 'grid' },
    { label: 'Employes', route: '/directeur-general/employes', icon: 'users' },
    { label: 'Nouvelle Demande', route: '/directeur-general/nouvelle-demande', icon: 'plus-circle' },
    { label: 'Mes Demandes', route: '/directeur-general/mes-demandes', icon: 'file-text' },
    { label: 'Demandes a valider', route: '/directeur-general/demandes-a-valider', icon: 'file-text' },
    { label: 'Demandes validees', route: '/directeur-general/demandes-validees', icon: 'check-circle' },
    { label: 'Historique', route: '/directeur-general/historique', icon: 'clock' },
    { label: 'Profil', route: '/directeur-general/profil', icon: 'user' },
  ];

  toggleSidebar() {
    this.isSidebarCollapsed.update(v => !v);
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigateByUrl('/auth/login');
    });
  }

  getIcon(name: string): string {
    const icons: Record<string, string> = {
      'grid': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>`,
      'file-text': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>`,
      'plus-circle': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>`,
      'clock': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`,
      'user': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
      'users': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
      'check-circle': `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`,
    };

    return icons[name] ?? '';
  }
}
