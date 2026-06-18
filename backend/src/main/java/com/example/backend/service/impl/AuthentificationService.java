package com.example.backend.service.impl;

import com.example.backend.dto.auth.AuthUserResponse;
import com.example.backend.exception.AuthException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.interfaces.IAuthentificationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuthentificationService implements IAuthentificationService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthentificationService(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthUserResponse login(String email, String password) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED));

        if (!utilisateur.isActif() || !passwordEncoder.matches(password, utilisateur.getPassword())) {
            throw new AuthException("Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(utilisateur);
        currentResponse().addHeader(HttpHeaders.SET_COOKIE, buildCookie(token, jwtService.getExpiration()).toString());

        return toAuthUserResponse(utilisateur);
    }

    @Override
    public void logout() {
        currentResponse().addHeader(HttpHeaders.SET_COOKIE, buildCookie("", 0).toString());
        SecurityContextHolder.clearContext();
    }

    @Override
    public AuthUserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("Utilisateur non authentifie", HttpStatus.UNAUTHORIZED);
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AuthException("Utilisateur introuvable", HttpStatus.UNAUTHORIZED));

        return toAuthUserResponse(utilisateur);
    }

    private AuthUserResponse toAuthUserResponse(Utilisateur utilisateur) {
        return employeRepository.findByUtilisateurId(utilisateur.getId())
                .map(employe -> responseFromEmploye(utilisateur, employe))
                .orElseGet(() -> defaultResponse(utilisateur));
    }

    private AuthUserResponse responseFromEmploye(Utilisateur utilisateur, Employe employe) {
        Departement departement = employe.getDepartement();
        return new AuthUserResponse(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                redirectUrl(utilisateur.getRole()),
                employe.getIdEmp(),
                employe.getNom(),
                employe.getPrenom(),
                employe.getMatricule(),
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getNom());
    }

    private AuthUserResponse defaultResponse(Utilisateur utilisateur) {
        if (utilisateur.getRole() == Role.ADMINISTRATEUR) {
            return new AuthUserResponse(
                    utilisateur.getId(),
                    utilisateur.getEmail(),
                    utilisateur.getRole(),
                    redirectUrl(utilisateur.getRole()),
                    null,
                    "Admin",
                    "Admin",
                    null,
                    null,
                    null);
        }

        return new AuthUserResponse(
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                redirectUrl(utilisateur.getRole()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private String redirectUrl(Role role) {
        return switch (role) {
            case EMPLOYE -> "/employe/dashboard";
            case RH -> "/rh/dashboard";
            case RESPONSABLE -> "/responsable/dashboard";
            case DIRECTEUR_GENERAL -> "/directeur-general/dashboard";
            case ADMINISTRATEUR -> "/admin/dashboard";
        };
    }

    private ResponseCookie buildCookie(String token, long maxAgeMillis) {
        return ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeMillis / 1000)
                .build();
    }

    private HttpServletResponse currentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getResponse();
    }
}
