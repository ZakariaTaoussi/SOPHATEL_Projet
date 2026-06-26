package com.example.backend.dto.rh;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RhDemandeSuiviResponse(
        Long id,
        String reference,
        Long employeId,
        Long utilisateurId,
        String employeNomComplet,
        String matricule,
        String email,
        Long departementId,
        String departementNom,
        String typeDemande,
        String natureConge,
        String status,
        LocalDate dateDebutDg,
        LocalDate dateFinDg,
        Double joursDeduits,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
