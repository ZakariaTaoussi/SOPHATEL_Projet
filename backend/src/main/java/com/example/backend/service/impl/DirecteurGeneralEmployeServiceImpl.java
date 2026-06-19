package com.example.backend.service.impl;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.directeurgeneral.DirecteurGeneralEmployeResponse;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.DirecteurGeneralEmployeMapper;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.IDirecteurGeneralEmployeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DirecteurGeneralEmployeServiceImpl implements IDirecteurGeneralEmployeService {

    private static final int DEFAULT_SIZE = 4;
    private static final int MAX_SIZE = 20;
    private static final Set<Role> ROLES_AFFICHABLES = Set.of(Role.EMPLOYE, Role.RH, Role.RESPONSABLE);

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final ISoldeCongeService soldeCongeService;
    private final DirecteurGeneralEmployeMapper directeurGeneralEmployeMapper;

    public DirecteurGeneralEmployeServiceImpl(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            ISoldeCongeService soldeCongeService,
            DirecteurGeneralEmployeMapper directeurGeneralEmployeMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.soldeCongeService = soldeCongeService;
        this.directeurGeneralEmployeMapper = directeurGeneralEmployeMapper;
    }

    @Override
    @Transactional
    public PageResponse<DirecteurGeneralEmployeResponse> getEmployes(
            int page,
            int size,
            String search,
            String role,
            Long departementId) {
        verifierDirecteurGeneralConnecte();

        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String searchPattern = normalizeSearchPattern(search);
        Role roleFiltre = normalizeRole(role);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        Integer annee = LocalDate.now().getYear();

        Page<DirecteurGeneralEmployeResponse> result = employeRepository.findEmployesForDirecteurGeneral(
                        ROLES_AFFICHABLES,
                        roleFiltre,
                        departementId,
                        searchPattern,
                        pageable)
                .map(employe -> directeurGeneralEmployeMapper.toResponse(employe, getSolde(employe, annee), annee));

        return PageResponse.from(result);
    }

    private void verifierDirecteurGeneralConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Utilisateur connecté introuvable.");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecté introuvable."));

        if (utilisateur.getRole() != Role.DIRECTEUR_GENERAL) {
            throw new ForbiddenException("Accès réservé au directeur général.");
        }
    }

    private int normalizePage(int page) {
        if (page < 0) {
            throw new BadRequestException("Paramètres de pagination invalides.");
        }
        return page;
    }

    private int normalizeSize(int size) {
        int normalizedSize = size <= 0 ? DEFAULT_SIZE : size;
        if (normalizedSize > MAX_SIZE) {
            throw new BadRequestException("Paramètres de pagination invalides.");
        }
        return normalizedSize;
    }

    private String normalizeSearchPattern(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }
        return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private Role normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        try {
            Role parsedRole = Role.valueOf(role.trim().toUpperCase());
            if (parsedRole == Role.ADMINISTRATEUR) {
                throw new BadRequestException("Les administrateurs ne sont pas affichés dans cette page.");
            }
            if (!ROLES_AFFICHABLES.contains(parsedRole)) {
                throw new BadRequestException("Rôle invalide.");
            }
            return parsedRole;
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Rôle invalide.");
        }
    }

    private SoldeConge getSolde(Employe employe, Integer annee) {
        if (employe == null || employe.getIdEmp() == null) {
            return null;
        }
        return soldeCongeService.getOrCreateSolde(employe.getIdEmp(), annee);
    }
}
