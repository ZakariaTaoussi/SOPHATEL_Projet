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
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, NavIconComponent],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['../employe-layout/employe-layout.component.scss'],
})
export class AdminLayoutComponent {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  isSidebarCollapsed = signal(false);

  get currentUser() {
    const user = this.authService.currentUser();
    return {
      nom: this.authService.getDisplayName(user) || 'Admin Admin',
      role: user?.role ?? Role.ADMINISTRATEUR,
      avatar: this.authService.getInitials(user) || 'AA',
      departement: 'Administration',
    };
  }

  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/admin/dashboard', icon: 'dashboard' },
    { label: 'Employes', route: '/admin/employes', icon: 'users' },
    { label: 'Departements', route: '/admin/departements', icon: 'building' },
    { label: 'Jour Ferie', route: '/admin/jour-ferie', icon: 'calendar-off' },
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
