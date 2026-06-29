package com.example.backend.service.interfaces;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.notification.NotificationResponse;
import com.example.backend.model.enums.NotificationType;

public interface INotificationService {
    PageResponse<NotificationResponse> getMyNotifications(int page, int size);

    long getUnreadCount();

    NotificationResponse markAsRead(Long notificationId);

    void markAllAsRead();

    void createNotification(
            Long recipientUserId,
            Long senderUserId,
            Long demandeId,
            NotificationType type,
            String title,
            String message,
            String targetUrl);
}
