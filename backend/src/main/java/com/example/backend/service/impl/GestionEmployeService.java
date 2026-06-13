package com.example.backend.service.impl;

import com.example.backend.dto.admin.CreateEmployeRequest;
import com.example.backend.dto.admin.EmployeResponse;
import com.example.backend.dto.admin.PageResponse;
import com.example.backend.dto.admin.UpdateEmployeRequest;
import com.example.backend.exception.BusinessException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatutEmploye;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.IGestionEmploye;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionEmployeService implements IGestionEmploye {

    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final PasswordEncoder passwordEncoder;

    public GestionEmployeService(
            EmployeRepository employeRepository,
            UtilisateurRepository utilisateurRepository,
            DepartementRepository departementRepository,
            PasswordEncoder passwordEncoder) {
        this.employeRepository = employeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.departementRepository = departementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public EmployeResponse creerEmploye(CreateEmployeRequest request) {
        validateRequired(request, true);
        if (utilisateurRepository.existsByEmail(request.getEmail().trim())) {
            throw new BusinessException("Email deja utilise", HttpStatus.CONFLICT);
        }
        if (employeRepository.existsByMatricule(request.getMatricule().trim())) {
            throw new BusinessException("Matricule deja utilise", HttpStatus.CONFLICT);
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(request.getEmail().trim());
        utilisateur.setRole(request.getRole());
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        utilisateur.setActif(request.getStatut() != StatutEmploye.INACTIF);
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        Employe employe = new Employe();
        applyValues(employe, request);
        employe.setUtilisateur(savedUser);
        Employe savedEmploye = employeRepository.save(employe);
        synchroniserResponsableDepartement(savedEmploye);
        return toResponse(savedEmploye);
    }

    @Override
    @Transactional
    public EmployeResponse modifierEmploye(Long id, UpdateEmployeRequest request) {
        validateRequired(request, false);
        Employe employe = findEmploye(id);
        Utilisateur utilisateur = employe.getUtilisateur();

        if (utilisateurRepository.existsByEmailAndIdNot(request.getEmail().trim(), utilisateur.getId())) {
            throw new BusinessException("Email deja utilise", HttpStatus.CONFLICT);
        }
        if (employeRepository.existsByMatriculeAndIdEmpNot(request.getMatricule().trim(), id)) {
            throw new BusinessException("Matricule deja utilise", HttpStatus.CONFLICT);
        }

        utilisateur.setEmail(request.getEmail().trim());
        utilisateur.setRole(request.getRole());
        utilisateur.setActif(request.getStatut() != StatutEmploye.INACTIF);
        if (!isBlank(request.getPassword())) {
            utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        utilisateurRepository.save(utilisateur);

        applyValues(employe, request);
        Employe savedEmploye = employeRepository.save(employe);
        synchroniserResponsableDepartement(savedEmploye);
        return toResponse(savedEmploye);
    }

    @Override
    @Transactional
    public void supprimerEmploye(Long id) {
        Employe employe = findEmploye(id);
        Utilisateur utilisateur = employe.getUtilisateur();
        nettoyerAnciennesResponsabilites(employe);
        employeRepository.delete(employe);
        utilisateurRepository.delete(utilisateur);
    }

    @Override
    public EmployeResponse consulterEmploye(Long id) {
        return toResponse(findEmploye(id));
    }

    @Override
    public PageResponse<EmployeResponse> consulterEmployes(String search, Pageable pageable) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        Page<EmployeResponse> page = (normalizedSearch == null
                ? employeRepository.findAll(pageable)
                : employeRepository.search(normalizedSearch, pageable))
                .map(this::toResponse);
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize());
    }

    private void validateRequired(CreateEmployeRequest request, boolean passwordRequired) {
        if (isBlank(request.getMatricule())) {
            throw new BusinessException("Le matricule est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (isBlank(request.getNom())) {
            throw new BusinessException("Le nom est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (isBlank(request.getPrenom())) {
            throw new BusinessException("Le prenom est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (isBlank(request.getEmail())) {
            throw new BusinessException("L'email est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (passwordRequired && isBlank(request.getPassword())) {
            throw new BusinessException("Le mot de passe est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (request.getRole() == null) {
            throw new BusinessException("Le role est obligatoire", HttpStatus.BAD_REQUEST);
        }
        if (request.getStatut() == null) {
            request.setStatut(StatutEmploye.ACTIF);
        }
    }

    private void applyValues(Employe employe, CreateEmployeRequest request) {
        employe.setMatricule(request.getMatricule().trim());
        employe.setNom(request.getNom().trim());
        employe.setPrenom(request.getPrenom().trim());
        employe.setStatut(request.getStatut());
        employe.setDepartement(findDepartementOrNull(request.getDepartementId()));
    }

    private Departement findDepartementOrNull(Long departementId) {
        if (departementId == null) {
            return null;
        }
        return departementRepository.findById(departementId)
                .orElseThrow(() -> new BusinessException("Departement introuvable", HttpStatus.NOT_FOUND));
    }

    private void synchroniserResponsableDepartement(Employe employe) {
        nettoyerAnciennesResponsabilites(employe);

        if (employe.getUtilisateur().getRole() != Role.RESPONSABLE || employe.getDepartement() == null) {
            return;
        }

        Departement departement = employe.getDepartement();
        departement.setResponsable(employe);
        departementRepository.save(departement);
    }

    private void nettoyerAnciennesResponsabilites(Employe employe) {
        departementRepository.clearResponsable(employe.getIdEmp());
    }

    private Employe findEmploye(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Employe introuvable", HttpStatus.NOT_FOUND));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private EmployeResponse toResponse(Employe employe) {
        Departement departement = employe.getDepartement();
        Utilisateur utilisateur = employe.getUtilisateur();
        Role role = utilisateur.getRole();
        return new EmployeResponse(
                employe.getIdEmp(),
                employe.getMatricule(),
                employe.getNom(),
                employe.getPrenom(),
                utilisateur.getEmail(),
                role,
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getNom(),
                employe.getStatut());
    }
}
