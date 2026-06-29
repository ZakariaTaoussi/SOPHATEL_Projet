package com.example.backend.mapper;

import com.example.backend.dto.notification.NotificationResponse;
import com.example.backend.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient() == null ? null : notification.getRecipient().getId(),
                notification.getSender() == null ? null : notification.getSender().getId(),
                notification.getDemande() == null ? null : notification.getDemande().getId(),
                notification.getType() == null ? null : notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getTargetUrl());
    }
}
