import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NotificationResponse, NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-shared-notification-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-page.component.html',
  styleUrls: ['./notification-page.component.scss'],
})
export class SharedNotificationPageComponent implements OnInit {
  notifications: NotificationResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  first = true;
  last = true;
  unreadCount = 0;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly notificationService: NotificationService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(page = this.page): void {
    this.loading = true;
    this.errorMessage = '';
    this.notificationService.getNotifications(page, this.size)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: response => {
          this.notifications = [...(response.content ?? [])];
          this.page = response.page ?? response.currentPage ?? page;
          this.size = response.size ?? this.size;
          this.totalElements = response.totalElements ?? 0;
          this.totalPages = response.totalPages ?? 0;
          this.first = response.first ?? this.page === 0;
          this.last = response.last ?? this.page + 1 >= this.totalPages;
          this.unreadCount = this.notifications.filter(notification => !notification.read).length;
          this.cdr.markForCheck();
        },
        error: error => {
          this.errorMessage = error?.error?.message || 'Erreur lors du chargement des notifications.';
          this.notifications = [];
          this.totalElements = 0;
          this.totalPages = 0;
          this.unreadCount = 0;
          this.cdr.markForCheck();
        },
      });
  }

  markAsRead(notification: NotificationResponse): void {
    if (notification.read) {
      this.navigate(notification);
      return;
    }
    this.notificationService.markAsRead(notification.id).subscribe({
      next: updated => {
        this.notifications = this.notifications.map(item => item.id === updated.id ? updated : item);
        this.unreadCount = this.notifications.filter(item => !item.read).length;
        this.cdr.markForCheck();
        this.navigate(updated);
      },
      error: () => {
        this.errorMessage = 'Impossible de marquer la notification comme lue.';
        this.cdr.markForCheck();
      },
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => this.loadNotifications(this.page),
      error: () => {
        this.errorMessage = 'Impossible de marquer toutes les notifications comme lues.';
        this.cdr.markForCheck();
      },
    });
  }

  previousPage(): void {
    if (this.page > 0) {
      this.loadNotifications(this.page - 1);
    }
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.loadNotifications(this.page + 1);
    }
  }

  kind(notification: NotificationResponse): 'info' | 'success' | 'warning' {
    if (notification.type.includes('REFUSED') || notification.type.includes('CANCELLED')) {
      return 'warning';
    }
    if (notification.type.includes('VALIDATED')) {
      return 'success';
    }
    return 'info';
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  private navigate(notification: NotificationResponse): void {
    if (notification.targetUrl) {
      this.router.navigateByUrl(notification.targetUrl);
    }
  }
}
