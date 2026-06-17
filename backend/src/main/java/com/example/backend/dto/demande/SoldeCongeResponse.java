package com.example.backend.dto.demande;

public class SoldeCongeResponse {
    private Long id;
    private Long empId;
    private Integer annee;
    private Double soldeActuel;
    private Double soldeTotal;

    public SoldeCongeResponse(Long id, Long empId, Integer annee, Double soldeActuel, Double soldeTotal) {
        this.id = id;
        this.empId = empId;
        this.annee = annee;
        this.soldeActuel = soldeActuel;
        this.soldeTotal = soldeTotal;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpId() {
        return empId;
    }

    public Integer getAnnee() {
        return annee;
    }

    public Double getSoldeActuel() {
        return soldeActuel;
    }

    public Double getSoldeTotal() {
        return soldeTotal;
    }
}
