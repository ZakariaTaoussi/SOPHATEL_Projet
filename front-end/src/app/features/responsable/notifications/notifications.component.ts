import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

type NotificationKind = 'info' | 'success' | 'warning';
type NotificationScope = 'personnelle' | 'equipe';

interface ResponsableNotification {
  title: string;
  date: string;
  kind: NotificationKind;
  scope: NotificationScope;
  read: boolean;
}

@Component({
  selector: 'app-responsable-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss'],
})
export class ResponsableNotificationsComponent {
  notifications: ResponsableNotification[] = [
    { title: 'Votre demande RESP-002 a été validée par le DG.', date: 'Aujourd’hui, 09:20', kind: 'success', scope: 'personnelle', read: false },
    { title: 'Votre demande RESP-004 a été refusée par le DG.', date: 'Hier, 17:40', kind: 'warning', scope: 'personnelle', read: true },
    { title: 'Ahmed Benali a soumis une demande de congé en attente de validation.', date: 'Aujourd’hui, 08:45', kind: 'info', scope: 'equipe', read: false },
    { title: 'Lina Mansouri a soumis une demande de rattrapage en attente de validation.', date: 'Hier, 16:10', kind: 'info', scope: 'equipe', read: false },
  ];

  get unreadNotifications(): ResponsableNotification[] {
    return this.notifications.filter(notification => !notification.read);
  }

  get notificationsPersonnelles(): ResponsableNotification[] {
    return this.notifications.filter(notification => notification.scope === 'personnelle');
  }

  get notificationsEquipe(): ResponsableNotification[] {
    return this.notifications.filter(notification => notification.scope === 'equipe');
  }

  markAllAsRead(): void {
    this.notifications = this.notifications.map(notification => ({ ...notification, read: true }));
  }
}
