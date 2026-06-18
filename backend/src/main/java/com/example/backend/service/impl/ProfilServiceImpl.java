package com.example.backend.service.impl;

import com.example.backend.dto.profil.ProfilResponse;
import com.example.backend.dto.profil.ProfilUpdateRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.IProfilService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfilServiceImpl implements IProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final ISoldeCongeService soldeCongeService;

    public ProfilServiceImpl(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            ISoldeCongeService soldeCongeService) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.soldeCongeService = soldeCongeService;
    }

    @Override
    @Transactional
    public ProfilResponse getProfilConnecte() {
        Utilisateur utilisateur = getUtilisateurConnecte();
        Employe employe = getEmployeLie(utilisateur);
        return buildResponse(utilisateur, employe);
    }

    @Override
    @Transactional
    public ProfilResponse updateProfilConnecte(ProfilUpdateRequest request) {
        Utilisateur utilisateur = getUtilisateurConnecte();
        Employe employe = getEmployeLie(utilisateur);

        String nom = normalizeNom(request == null ? null : request.getNom());
        String prenom = normalizePrenom(request == null ? null : request.getPrenom());

        employe.setNom(nom);
        employe.setPrenom(prenom);
        Employe saved = employeRepository.save(employe);
        return buildResponse(utilisateur, saved);
    }

    private Utilisateur getUtilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidBusinessRequestException("Profil introuvable");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidBusinessRequestException("Profil introuvable"));
        if (utilisateur.getRole() == Role.ADMINISTRATEUR) {
            throw new InvalidBusinessRequestException("Le profil employe n'est pas disponible pour l'administrateur");
        }
        return utilisateur;
    }

    private Employe getEmployeLie(Utilisateur utilisateur) {
        return employeRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new InvalidBusinessRequestException("Aucun employe lie a cet utilisateur"));
    }

    private ProfilResponse buildResponse(Utilisateur utilisateur, Employe employe) {
        Departement departement = employe.getDepartement();
        Employe manager = resolveManager(utilisateur.getRole(), departement);
        SoldeConge solde = soldeCongeService.getOrCreateSolde(employe.getIdEmp(), LocalDate.now().getYear());

        ProfilResponse response = new ProfilResponse();
        response.setUtilisateurId(utilisateur.getId());
        response.setEmployeId(employe.getIdEmp());
        response.setNom(employe.getNom());
        response.setPrenom(employe.getPrenom());
        response.setEmail(utilisateur.getEmail());
        response.setRole(utilisateur.getRole().name());
        response.setMatricule(employe.getMatricule());
        response.setDateEmbauche(employe.getCreatedAt());
        response.setDepartementId(departement == null ? null : departement.getId());
        response.setDepartementNom(departement == null ? null : departement.getNom());
        response.setManagerId(manager == null ? null : manager.getIdEmp());
        response.setManagerNomComplet(nomComplet(manager));
        response.setSoldeActuel(solde.getSoldeActuel());
        response.setSoldeTotal(solde.getSoldeTotal());
        response.setAnneeSolde(solde.getAnnee());
        return response;
    }

    private Employe resolveManager(Role role, Departement departement) {
        if (role == Role.DIRECTEUR_GENERAL) {
            return null;
        }
        if (role == Role.RESPONSABLE) {
            return employeRepository.findFirstByUtilisateurRole(Role.DIRECTEUR_GENERAL).orElse(null);
        }
        if (departement == null || departement.getId() == null) {
            return null;
        }
        return employeRepository.findFirstByDepartementIdAndUtilisateurRole(departement.getId(), Role.RESPONSABLE)
                .orElse(null);
    }

    private String normalizeNom(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidBusinessRequestException("Nom obligatoire");
        }
        return value.trim();
    }

    private String normalizePrenom(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidBusinessRequestException("Prenom obligatoire");
        }
        return value.trim();
    }

    private String nomComplet(Employe employe) {
        if (employe == null) {
            return null;
        }
        String prenom = employe.getPrenom() == null ? "" : employe.getPrenom().trim();
        String nom = employe.getNom() == null ? "" : employe.getNom().trim();
        String nomComplet = (prenom + " " + nom).trim();
        return nomComplet.isEmpty() ? null : nomComplet;
    }
}
