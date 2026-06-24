import { Component, signal } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Role } from '../../core/enums/role.enum';
import { AuthService } from '../../core/services/auth.service';
import { NavIconComponent } from '../../shared/nav-icon/nav-icon.component';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-rh-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, NavIconComponent],
  templateUrl: './rh-layout.component.html',
  styleUrls: ['./rh-layout.component.scss'],
})
export class RhLayoutComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  isSidebarCollapsed = signal(false);

  get currentUser() {
    const user = this.authService.currentUser();
    return {
      nom: this.authService.getDisplayName(user),
      role: user?.role ?? Role.RH,
      avatar: this.authService.getInitials(user) || 'RH',
      departement: user?.departementNom ?? 'Non defini',
    };
  }

  navItems: NavItem[] = [
    { label: 'Dashboard',       route: '/rh/dashboard',         icon: 'grid' },
    { label: 'Employes',        route: '/rh/Employes',          icon: 'users' },
    { label: 'Mes Demandes',    route: '/rh/mes-demandes',      icon: 'file-text' },
    { label: 'Mes Absences',    route: '/rh/mes-absences',      icon: 'calendar-off' },
    { label: 'Nouvelle Demande',route: '/rh/nouvelle-demande',  icon: 'plus-circle' },
    { label: 'Notification',    route: '/rh/notification',      icon: 'bell' },
    { label: 'Profil',          route: '/rh/profil',            icon: 'user' },
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
