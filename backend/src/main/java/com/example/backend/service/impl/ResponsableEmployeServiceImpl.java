package com.example.backend.service.impl;

import com.example.backend.dto.responsable.ResponsableEmployeResponse;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.ResponsableEmployeMapper;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.IResponsableEmployeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResponsableEmployeServiceImpl implements IResponsableEmployeService {

    private static final Set<Role> ROLES_EQUIPE = Set.of(Role.EMPLOYE, Role.RH);

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final ISoldeCongeService soldeCongeService;
    private final ResponsableEmployeMapper responsableEmployeMapper;

    public ResponsableEmployeServiceImpl(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            ISoldeCongeService soldeCongeService,
            ResponsableEmployeMapper responsableEmployeMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.soldeCongeService = soldeCongeService;
        this.responsableEmployeMapper = responsableEmployeMapper;
    }

    @Override
    @Transactional
    public List<ResponsableEmployeResponse> getMesEmployes() {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsable(responsable);
        Integer annee = LocalDate.now().getYear();

        return employeRepository.findEmployesEquipeResponsable(departement.getId(), responsable.getIdEmp(), ROLES_EQUIPE)
                .stream()
                .map(employe -> responsableEmployeMapper.toResponse(employe, getSolde(employe, annee), annee))
                .toList();
    }

    @Override
    @Transactional
    public ResponsableEmployeResponse getEmployeDeMonDepartement(Long employeId) {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsable(responsable);
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable."));

        Departement departementEmploye = employe.getDepartement();
        if (departementEmploye == null
                || departementEmploye.getId() == null
                || !departementEmploye.getId().equals(departement.getId())
                || employe.getIdEmp().equals(responsable.getIdEmp())) {
            throw new ForbiddenException("Vous n’avez pas le droit d’accéder à cet employé.");
        }

        Integer annee = LocalDate.now().getYear();
        return responsableEmployeMapper.toResponse(employe, getSolde(employe, annee), annee);
    }

    private Employe getResponsableConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Utilisateur connecté introuvable.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecté introuvable."));

        if (utilisateur.getRole() != Role.RESPONSABLE) {
            throw new ForbiddenException("Accès réservé au responsable.");
        }

        return employeRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new BadRequestException("Utilisateur connecté sans employé lié."));
    }

    private Departement getDepartementResponsable(Employe responsable) {
        Departement departement = responsable.getDepartement();
        if (departement == null || departement.getId() == null) {
            throw new BadRequestException("Responsable sans département.");
        }
        return departement;
    }

    private SoldeConge getSolde(Employe employe, Integer annee) {
        if (employe == null || employe.getIdEmp() == null) {
            return null;
        }
        return soldeCongeService.getOrCreateSolde(employe.getIdEmp(), annee);
    }
}
