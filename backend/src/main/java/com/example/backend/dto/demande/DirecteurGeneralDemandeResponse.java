package com.example.backend.dto.demande;

import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DirecteurGeneralDemandeResponse {
    private Long id;
    private Long empId;
    private String employeNomComplet;
    private Long departementId;
    private String departementNom;
    private String responsableNomComplet;
    private LocalDate dateDebutEmp;
    private LocalDate dateFinEmp;
    private LocalDate dateDebutResp;
    private LocalDate dateFinResp;
    private LocalDate dateDebutDg;
    private LocalDate dateFinDg;
    private TypeDemande typeDemande;
    private StatusDemande status;
    private Double joursDeduits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DirecteurGeneralDemandeResponse(
            Long id,
            Long empId,
            String employeNomComplet,
            Long departementId,
            String departementNom,
            String responsableNomComplet,
            LocalDate dateDebutEmp,
            LocalDate dateFinEmp,
            LocalDate dateDebutResp,
            LocalDate dateFinResp,
            LocalDate dateDebutDg,
            LocalDate dateFinDg,
            TypeDemande typeDemande,
            StatusDemande status,
            Double joursDeduits,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.empId = empId;
        this.employeNomComplet = employeNomComplet;
        this.departementId = departementId;
        this.departementNom = departementNom;
        this.responsableNomComplet = responsableNomComplet;
        this.dateDebutEmp = dateDebutEmp;
        this.dateFinEmp = dateFinEmp;
        this.dateDebutResp = dateDebutResp;
        this.dateFinResp = dateFinResp;
        this.dateDebutDg = dateDebutDg;
        this.dateFinDg = dateFinDg;
        this.typeDemande = typeDemande;
        this.status = status;
        this.joursDeduits = joursDeduits;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpId() {
        return empId;
    }

    public String getEmployeNomComplet() {
        return employeNomComplet;
    }

    public Long getDepartementId() {
        return departementId;
    }

    public String getDepartementNom() {
        return departementNom;
    }

    public String getResponsableNomComplet() {
        return responsableNomComplet;
    }

    public LocalDate getDateDebutEmp() {
        return dateDebutEmp;
    }

    public LocalDate getDateFinEmp() {
        return dateFinEmp;
    }

    public LocalDate getDateDebutResp() {
        return dateDebutResp;
    }

    public LocalDate getDateFinResp() {
        return dateFinResp;
    }

    public LocalDate getDateDebutDg() {
        return dateDebutDg;
    }

    public LocalDate getDateFinDg() {
        return dateFinDg;
    }

    public TypeDemande getTypeDemande() {
        return typeDemande;
    }

    public StatusDemande getStatus() {
        return status;
    }

    public Double getJoursDeduits() {
        return joursDeduits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
