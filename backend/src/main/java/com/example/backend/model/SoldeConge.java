package com.example.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "soldes_conge",
        uniqueConstraints = @UniqueConstraint(name = "uk_solde_conge_emp_annee", columnNames = {"emp_id", "annee"})
)
public class SoldeConge {

    private static final double SOLDE_ANNUEL_PAR_DEFAUT = 18D;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employe employe;

    @Column(nullable = false)
    private Integer annee;

    @Column(nullable = false)
    private Double soldeActuel;

    @Column(nullable = false)
    private Double soldeTotal;

    @PrePersist
    void prePersist() {
        if (soldeTotal == null) {
            soldeTotal = SOLDE_ANNUEL_PAR_DEFAUT;
        }
        if (soldeActuel == null) {
            soldeActuel = SOLDE_ANNUEL_PAR_DEFAUT;
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

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public Double getSoldeActuel() {
        return soldeActuel;
    }

    public void setSoldeActuel(Double soldeActuel) {
        this.soldeActuel = soldeActuel;
    }

    public Double getSoldeTotal() {
        return soldeTotal;
    }

    public void setSoldeTotal(Double soldeTotal) {
        this.soldeTotal = soldeTotal;
    }
}
