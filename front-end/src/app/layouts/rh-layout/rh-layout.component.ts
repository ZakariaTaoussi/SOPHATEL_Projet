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
  selector: 'app-rh-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, NavIconComponent],
  templateUrl: './rh-layout.component.html',
  styleUrls: ['./rh-layout.component.scss'],
})
export class RhLayoutComponent implements OnInit, OnDestroy {
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
      role: user?.role ?? Role.RH,
      avatar: this.authService.getInitials(user) || 'RH',
      departement: user?.departementNom ?? 'Non defini',
    };
  }

  navItems: NavItem[] = [
    { label: 'Dashboard',       route: '/rh/dashboard',         icon: 'grid' },
    { label: 'Conges employes', route: '/rh/employes/conges-valides', icon: 'users' },
    { label: 'Absences employes', route: '/rh/employes/absences-valides', icon: 'calendar-off' },
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
