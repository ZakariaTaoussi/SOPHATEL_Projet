import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, timeout } from 'rxjs';
import { apiUrl } from '../config/api-url';
import { PageResponse } from '../models/page-response.model';

export type NotificationKind = 'info' | 'success' | 'warning';

export interface NotificationResponse {
  id: number;
  recipientId?: number;
  senderId?: number;
  demandeId?: number;
  type: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  readAt?: string | null;
  targetUrl?: string | null;
}

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
  private readonly apiBase = apiUrl('/api/notifications');
  private nextId = 1;
  private readonly localItems: NotificationItem[] = [];

  constructor(private readonly http: HttpClient) {}

  getNotifications(page = 0, size = 10): Observable<PageResponse<NotificationResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<NotificationResponse>>(this.apiBase, {
      params,
      withCredentials: true,
    }).pipe(timeout(10000));
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiBase}/unread-count`, { withCredentials: true })
      .pipe(timeout(10000));
  }

  markAsRead(id: number): Observable<NotificationResponse> {
    return this.http.put<NotificationResponse>(`${this.apiBase}/${id}/read`, {}, { withCredentials: true })
      .pipe(timeout(10000));
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.apiBase}/read-all`, {}, { withCredentials: true })
      .pipe(timeout(10000));
  }

  add(title: string, kind: NotificationKind = 'info'): void {
    this.localItems.unshift({
      id: this.nextId++,
      title,
      date: this.formatToday(),
      kind,
      read: false,
    });
  }

  getLocalNotifications(): NotificationItem[] {
    return this.localItems;
  }

  private formatToday(): string {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    }).format(new Date());
  }
}
