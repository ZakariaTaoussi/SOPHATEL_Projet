package com.example.backend.dto.directeurgeneral;

import java.time.LocalDateTime;

public class DirecteurGeneralEmployeResponse {
    private Long id;
    private Long utilisateurId;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String matricule;
    private Long departementId;
    private String departementNom;
    private LocalDateTime dateEmbauche;
    private Double soldeActuel;
    private Double soldeTotal;
    private Integer anneeSolde;

    public DirecteurGeneralEmployeResponse(
            Long id,
            Long utilisateurId,
            String nom,
            String prenom,
            String email,
            String role,
            String matricule,
            Long departementId,
            String departementNom,
            LocalDateTime dateEmbauche,
            Double soldeActuel,
            Double soldeTotal,
            Integer anneeSolde) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.matricule = matricule;
        this.departementId = departementId;
        this.departementNom = departementNom;
        this.dateEmbauche = dateEmbauche;
        this.soldeActuel = soldeActuel;
        this.soldeTotal = soldeTotal;
        this.anneeSolde = anneeSolde;
    }

    public Long getId() {
        return id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getMatricule() {
        return matricule;
    }

    public Long getDepartementId() {
        return departementId;
    }

    public String getDepartementNom() {
        return departementNom;
    }

    public LocalDateTime getDateEmbauche() {
        return dateEmbauche;
    }

    public Double getSoldeActuel() {
        return soldeActuel;
    }

    public Double getSoldeTotal() {
        return soldeTotal;
    }

    public Integer getAnneeSolde() {
        return anneeSolde;
    }
}
