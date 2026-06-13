package com.example.backend.dto.admin;

import java.time.LocalDate;

public record JourFerieResponse(
        Long id,
        String nom,
        LocalDate dateDebut,
        LocalDate dateFin,
        String description) {
}
