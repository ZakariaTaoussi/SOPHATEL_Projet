package com.example.backend.dto.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long recipientId,
        Long senderId,
        Long demandeId,
        String type,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        String targetUrl) {
}
