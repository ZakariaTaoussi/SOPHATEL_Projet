package com.example.backend.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DashboardRecentDemandeDto(
        Long id,
        String reference,
        String typeDemande,
        String status,
        String employeNomComplet,
        String departementNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        Double joursDeduits,
        LocalDateTime createdAt) {
}
