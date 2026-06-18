package com.example.backend.dto.profil;

import java.time.LocalDateTime;

public class ProfilResponse {
    private Long utilisateurId;
    private Long employeId;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String matricule;
    private LocalDateTime dateEmbauche;
    private Long departementId;
    private String departementNom;
    private Long managerId;
    private String managerNomComplet;
    private Double soldeActuel;
    private Double soldeTotal;
    private Integer anneeSolde;

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Long getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Long employeId) {
        this.employeId = employeId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public LocalDateTime getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDateTime dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public Long getDepartementId() {
        return departementId;
    }

    public void setDepartementId(Long departementId) {
        this.departementId = departementId;
    }

    public String getDepartementNom() {
        return departementNom;
    }

    public void setDepartementNom(String departementNom) {
        this.departementNom = departementNom;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerNomComplet() {
        return managerNomComplet;
    }

    public void setManagerNomComplet(String managerNomComplet) {
        this.managerNomComplet = managerNomComplet;
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

    public Integer getAnneeSolde() {
        return anneeSolde;
    }

    public void setAnneeSolde(Integer anneeSolde) {
        this.anneeSolde = anneeSolde;
    }
}
