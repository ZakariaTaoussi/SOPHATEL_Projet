package com.example.backend.dto.demande;

import com.example.backend.model.enums.NatureConge;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeCongeImpressionResponse {
    private final Long id;
    private final String nom;
    private final String prenom;
    private final String fonction;
    private final String service;
    private final TypeDemande typeDemande;
    private final NatureConge natureConge;
    private final LocalDate dateDebutEmp;
    private final LocalDate dateFinEmp;
    private final LocalDate dateDebutDg;
    private final LocalDate dateFinDg;
    private final Double joursDeduits;
    private final Integer anneeDebut;
    private final Integer anneeFin;
    private final Double reliquatConge;
    private final StatusDemande status;
    private final String statutDemandeur;
    private final String statutResponsable;
    private final String statutDirecteurGeneral;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public DemandeCongeImpressionResponse(
            Long id,
            String nom,
            String prenom,
            String fonction,
            String service,
            TypeDemande typeDemande,
            NatureConge natureConge,
            LocalDate dateDebutEmp,
            LocalDate dateFinEmp,
            LocalDate dateDebutDg,
            LocalDate dateFinDg,
            Double joursDeduits,
            Integer anneeDebut,
            Integer anneeFin,
            Double reliquatConge,
            StatusDemande status,
            String statutDemandeur,
            String statutResponsable,
            String statutDirecteurGeneral,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.fonction = fonction;
        this.service = service;
        this.typeDemande = typeDemande;
        this.natureConge = natureConge;
        this.dateDebutEmp = dateDebutEmp;
        this.dateFinEmp = dateFinEmp;
        this.dateDebutDg = dateDebutDg;
        this.dateFinDg = dateFinDg;
        this.joursDeduits = joursDeduits;
        this.anneeDebut = anneeDebut;
        this.anneeFin = anneeFin;
        this.reliquatConge = reliquatConge;
        this.status = status;
        this.statutDemandeur = statutDemandeur;
        this.statutResponsable = statutResponsable;
        this.statutDirecteurGeneral = statutDirecteurGeneral;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getFonction() { return fonction; }
    public String getService() { return service; }
    public TypeDemande getTypeDemande() { return typeDemande; }
    public NatureConge getNatureConge() { return natureConge; }
    public LocalDate getDateDebutEmp() { return dateDebutEmp; }
    public LocalDate getDateFinEmp() { return dateFinEmp; }
    public LocalDate getDateDebutDg() { return dateDebutDg; }
    public LocalDate getDateFinDg() { return dateFinDg; }
    public Double getJoursDeduits() { return joursDeduits; }
    public Integer getAnneeDebut() { return anneeDebut; }
    public Integer getAnneeFin() { return anneeFin; }
    public Double getReliquatConge() { return reliquatConge; }
    public StatusDemande getStatus() { return status; }
    public String getStatutDemandeur() { return statutDemandeur; }
    public String getStatutResponsable() { return statutResponsable; }
    public String getStatutDirecteurGeneral() { return statutDirecteurGeneral; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
