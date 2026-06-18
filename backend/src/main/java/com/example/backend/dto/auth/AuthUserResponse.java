package com.example.backend.dto.auth;

import com.example.backend.model.enums.Role;

public class AuthUserResponse {

    private Long id;
    private String email;
    private Role role;
    private String redirectUrl;
    private Long employeId;
    private String nom;
    private String prenom;
    private String matricule;
    private Long departementId;
    private String departementNom;

    public AuthUserResponse(
            Long id,
            String email,
            Role role,
            String redirectUrl,
            Long employeId,
            String nom,
            String prenom,
            String matricule,
            Long departementId,
            String departementNom) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.redirectUrl = redirectUrl;
        this.employeId = employeId;
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.departementId = departementId;
        this.departementNom = departementNom;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public Long getEmployeId() {
        return employeId;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
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
}
