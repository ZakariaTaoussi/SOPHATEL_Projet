package com.example.backend.model;

import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_conge")
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employe employe;

    @Column(nullable = false)
    private LocalDate dateDebutEmp;

    @Column(nullable = false)
    private LocalDate dateFinEmp;

    private LocalDate dateDebutResp;

    private LocalDate dateFinResp;

    private LocalDate dateDebutDg;

    private LocalDate dateFinDg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDemande typeDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDemande status = StatusDemande.BROUILLON;

    @Column(nullable = false)
    private Double joursDeduits = 0D;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = StatusDemande.BROUILLON;
        }
        if (joursDeduits == null) {
            joursDeduits = 0D;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (joursDeduits == null) {
            joursDeduits = 0D;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public LocalDate getDateDebutEmp() {
        return dateDebutEmp;
    }

    public void setDateDebutEmp(LocalDate dateDebutEmp) {
        this.dateDebutEmp = dateDebutEmp;
    }

    public LocalDate getDateFinEmp() {
        return dateFinEmp;
    }

    public void setDateFinEmp(LocalDate dateFinEmp) {
        this.dateFinEmp = dateFinEmp;
    }

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

    public TypeDemande getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(TypeDemande typeDemande) {
        this.typeDemande = typeDemande;
    }

    public StatusDemande getStatus() {
        return status;
    }

    public void setStatus(StatusDemande status) {
        this.status = status;
    }

    public Double getJoursDeduits() {
        return joursDeduits;
    }

    public void setJoursDeduits(Double joursDeduits) {
        this.joursDeduits = joursDeduits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
