package com.example.backend.dto.admin;

import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatutEmploye;

public record EmployeResponse(
        Long id,
        String matricule,
        String nom,
        String prenom,
        String email,
        Role role,
        Long departementId,
        String departementNom,
        StatutEmploye statut) {
}
