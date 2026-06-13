package com.example.backend.dto.auth;

import com.example.backend.model.enums.Role;

public class AuthUserResponse {

    private Long id;
    private String email;
    private Role role;
    private String nom;
    private String prenom;

    public AuthUserResponse(Long id, String email, Role role, String nom, String prenom) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
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

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
}
