package com.example.backend.dto.dashboard;

import java.time.LocalDateTime;

public record DashboardRecentEmployeDto(
        Long id,
        String matricule,
        String nomComplet,
        String email,
        String role,
        String departementNom,
        LocalDateTime createdAt) {
}
