package com.example.backend.service.impl;

import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class EmployeConnecteProvider {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;

    public EmployeConnecteProvider(UtilisateurRepository utilisateurRepository, EmployeRepository employeRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
    }

    public Employe getEmployeConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidBusinessRequestException("Utilisateur non authentifie");
        }

        String email = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidBusinessRequestException("Utilisateur connecte introuvable"));

        return employeRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new InvalidBusinessRequestException("Utilisateur connecte sans Employe lie"));
    }
}
