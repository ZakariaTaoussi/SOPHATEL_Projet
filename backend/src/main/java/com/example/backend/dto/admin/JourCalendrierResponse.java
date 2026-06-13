package com.example.backend.dto.admin;

import java.time.LocalDate;

public record JourCalendrierResponse(
        Long id,
        LocalDate date,
        Long jourFerieId,
        String jourFerieNom,
        String description) {
}
