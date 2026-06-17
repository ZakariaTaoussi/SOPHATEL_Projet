package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures_demande")
public class SignatureDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_demande", nullable = false, unique = true)
    private DemandeConge demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resp_id")
    private Employe responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dg_id")
    private Employe directeurGeneral;

    private LocalDateTime dateSignatureEmp;

    private LocalDateTime dateSignatureResp;

    private LocalDateTime dateSignatureDg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DemandeConge getDemande() {
        return demande;
    }

    public void setDemande(DemandeConge demande) {
        this.demande = demande;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    public Employe getResponsable() {
        return responsable;
    }

    public void setResponsable(Employe responsable) {
        this.responsable = responsable;
    }

    public Employe getDirecteurGeneral() {
        return directeurGeneral;
    }

    public void setDirecteurGeneral(Employe directeurGeneral) {
        this.directeurGeneral = directeurGeneral;
    }

    public LocalDateTime getDateSignatureEmp() {
        return dateSignatureEmp;
    }

    public void setDateSignatureEmp(LocalDateTime dateSignatureEmp) {
        this.dateSignatureEmp = dateSignatureEmp;
    }

    public LocalDateTime getDateSignatureResp() {
        return dateSignatureResp;
    }

    public void setDateSignatureResp(LocalDateTime dateSignatureResp) {
        this.dateSignatureResp = dateSignatureResp;
    }

    public LocalDateTime getDateSignatureDg() {
        return dateSignatureDg;
    }

    public void setDateSignatureDg(LocalDateTime dateSignatureDg) {
        this.dateSignatureDg = dateSignatureDg;
    }
}
