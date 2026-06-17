package com.example.backend.dto.demande;

import java.time.LocalDate;

public class DirecteurGeneralValidationDemandeRequest {
    private LocalDate dateDebutDg;
    private LocalDate dateFinDg;

    public LocalDate getDateDebutDg() {
        return dateDebutDg;
    }

    public void setDateDebutDg(LocalDate dateDebutDg) {
        this.dateDebutDg = dateDebutDg;
    }

    public LocalDate getDateFinDg() {
        return dateFinDg;
    }

    public void setDateFinDg(LocalDate dateFinDg) {
        this.dateFinDg = dateFinDg;
    }
}
