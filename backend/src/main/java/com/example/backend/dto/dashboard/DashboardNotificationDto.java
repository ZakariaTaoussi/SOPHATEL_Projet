package com.example.backend.dto.dashboard;

import java.time.LocalDateTime;

public record DashboardNotificationDto(
        Long id,
        String title,
        String message,
        boolean read,
        String targetUrl,
        LocalDateTime createdAt) {
}
