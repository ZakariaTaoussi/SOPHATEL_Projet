package com.example.backend.dto.admin;

public record DepartementResponse(
        Long id,
        String nom,
        Long responsableId,
        String responsableNomComplet,
        Integer nombreEmployes) {
}
