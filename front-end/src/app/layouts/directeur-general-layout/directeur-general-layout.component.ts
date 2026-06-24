import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Role } from '../../core/enums/role.enum';
import { AuthService } from '../../core/services/auth.service';
import { NavIconComponent } from '../../shared/nav-icon/nav-icon.component';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-directeur-general-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, NavIconComponent],
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
}
