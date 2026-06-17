package com.example.backend.dto.demande;

import java.time.LocalDate;

public class ResponsableValidationDemandeRequest {
    private LocalDate dateDebutResp;
    private LocalDate dateFinResp;

    public LocalDate getDateDebutResp() {
        return dateDebutResp;
    }

    public void setDateDebutResp(LocalDate dateDebutResp) {
        this.dateDebutResp = dateDebutResp;
    }

    public LocalDate getDateFinResp() {
        return dateFinResp;
    }

    public void setDateFinResp(LocalDate dateFinResp) {
        this.dateFinResp = dateFinResp;
    }
}
