import { Component, signal } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Role } from '../../core/enums/role.enum';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { NavIconComponent } from '../../shared/nav-icon/nav-icon.component';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-employe-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, NavIconComponent],
  templateUrl: './employe-layout.component.html',
  styleUrls: ['./employe-layout.component.scss'],
})
export class EmployeLayoutComponent {
  constructor(
    private readonly notificationService: NotificationService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  isSidebarCollapsed = signal(false);

  get currentUser() {
    const user = this.authService.currentUser();
    return {
      nom: this.authService.getDisplayName(user),
      role: user?.role ?? Role.EMPLOYE,
      avatar: this.authService.getInitials(user) || 'UT',
      departement: user?.departementNom ?? 'Non defini',
    };
  }

  navItems: NavItem[] = [
    { label: 'Dashboard',       route: '/employe/dashboard',        icon: 'grid' },
    { label: 'Mes Absences',    route: '/employe/mes-absences',     icon: 'calendar-off' },
    { label: 'Mes Demandes',    route: '/employe/mes-demandes',     icon: 'file-text' },
    { label: 'Nouvelle Demande',route: '/employe/nouvelle-demande', icon: 'plus-circle' },
    { label: 'Notification',    route: '/employe/notification',      icon: 'bell' },
    { label: 'Profil',          route: '/employe/profil',           icon: 'user' },
  ];

  toggleSidebar() {
    this.isSidebarCollapsed.update(v => !v);
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.router.navigateByUrl('/auth/login');
    });
  }

  get notificationCount(): number {
    return this.notificationService.getUnreadCount();
  }
}
