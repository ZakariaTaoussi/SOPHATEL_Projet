import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationItem, NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
})
export class NotificationComponent {
  constructor(private readonly notificationService: NotificationService) {}

  get notifications(): NotificationItem[] {
    return this.notificationService.getNotifications();
  }

  get unreadNotifications(): NotificationItem[] {
    return this.notifications.filter(notification => !notification.read);
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead();
  }
}
