import { Injectable } from '@angular/core';

export type NotificationKind = 'info' | 'success' | 'warning';

export type NotificationItem = {
  id: number;
  title: string;
  date: string;
  kind: NotificationKind;
  read: boolean;
};

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private nextId = 4;
  private readonly items: NotificationItem[] = [
    {
      id: 1,
      title: 'Votre demande de congé du 04/05 au 15/05 est en attente de validation.',
      date: '20 avril 2026',
      kind: 'info',
      read: false,
    },
    {
      id: 2,
      title: 'Votre demande de rattrapage a été validée par votre responsable.',
      date: '22 avril 2026',
      kind: 'success',
      read: false,
    },
    {
      id: 3,
      title: 'Votre demande DEM-005 a été refusée.',
      date: '26 avril 2026',
      kind: 'warning',
      read: false,
    },
  ];

  getNotifications(): NotificationItem[] {
    return this.items;
  }

  getUnreadCount(): number {
    return this.items.filter(item => !item.read).length;
  }

  add(title: string, kind: NotificationKind = 'info'): void {
    this.items.unshift({
      id: this.nextId++,
      title,
      date: this.formatToday(),
      kind,
      read: false,
    });
  }

  markAllAsRead(): void {
    this.items.forEach(item => item.read = true);
  }

  private formatToday(): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    }).format(new Date());
  }
}
