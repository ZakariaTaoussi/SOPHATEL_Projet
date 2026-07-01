import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subscription, catchError, interval, of, startWith, switchMap } from 'rxjs';
import { Role } from '../../core/enums/role.enum';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { NavIconComponent } from '../../shared/nav-icon/nav-icon.component';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-responsable-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, NavIconComponent],
  templateUrl: './responsable-layout.component.html',
  styleUrls: ['./responsable-layout.component.scss'],
})
export class ResponsableLayoutComponent implements OnInit, OnDestroy {
  notificationCount = 0;
  private notificationPolling?: Subscription;

  constructor(
    private readonly authService: AuthService,
    private readonly notificationService: NotificationService,
    private readonly router: Router
  ) {}

  isSidebarCollapsed = signal(false);

  ngOnInit(): void {
    this.notificationPolling = interval(10000).pipe(
      startWith(0),
      switchMap(() => this.notificationService.getUnreadCount().pipe(catchError(() => of({ count: 0 }))))
    ).subscribe(response => this.notificationCount = response.count);
  }

  ngOnDestroy(): void {
    this.notificationPolling?.unsubscribe();
  }

  get currentUser() {
    const user = this.authService.currentUser();
    return {
      nom: this.authService.getDisplayName(user),
      role: user?.role ?? Role.RESPONSABLE,
      avatar: this.authService.getInitials(user) || 'RS',
      departement: user?.departementNom ?? 'Non defini',
    };
  }

  navItems: NavItem[] = [
    { label: 'Dashboard',           route: '/responsable/dashboard',         icon: 'grid' },
    { label: 'Mes Employés',        route: '/responsable/mes-employes',      icon: 'users' },
    { label: 'Demandes a valider',  route: '/responsable/demandes-a-valider', icon: 'check-circle' },
    { label: 'Mes Demandes',        route: '/responsable/mes-demandes',      icon: 'file-text' },
    { label: 'Mes Absences',        route: '/responsable/mes-absences',      icon: 'calendar-off' },
    { label: 'Nouvelle Demande',    route: '/responsable/nouvelle-demande',  icon: 'plus-circle' },
    { label: 'Notifications',       route: '/responsable/notifications',     icon: 'bell' },
    { label: 'Profil',              route: '/responsable/profil',            icon: 'user' },
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
