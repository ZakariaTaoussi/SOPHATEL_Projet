import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

type NotificationKind = 'info' | 'success' | 'warning';

interface RhNotification {
  title: string;
  date: string;
  kind: NotificationKind;
  read: boolean;
}

@Component({
  selector: 'app-rh-notification',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss'],
})
export class RhNotificationComponent {
  notifications: RhNotification[] = [
    {
      title: 'Votre demande RH-002 a été acceptée par le Directeur Général.',
      date: 'Aujourd’hui, 09:20',
      kind: 'success',
      read: false,
    },
    {
      title: 'Nouvelle demande de congé validée DG pour Ahmed Benali.',
      date: 'Aujourd’hui, 08:45',
      kind: 'info',
      read: false,
    },
    {
      title: 'Nouvelle demande de rattrapage validée DG pour Lina Mansouri.',
      date: 'Hier, 16:10',
      kind: 'info',
      read: false,
    },
    {
      title: 'Youssef Idrissi a déclaré une absence avec justificatif.',
      date: 'Hier, 11:35',
      kind: 'warning',
      read: true,
    },
    {
      title: 'Nadia El Fassi a déclaré une absence maladie.',
      date: '12 mai 2026, 10:05',
      kind: 'warning',
      read: true,
    },
  ];

  get unreadNotifications(): RhNotification[] {
    return this.notifications.filter(notification => !notification.read);
  }

  markAllAsRead(): void {
    this.notifications = this.notifications.map(notification => ({ ...notification, read: true }));
  }
}
