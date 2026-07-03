package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.backend.service.impl.AuthentificationService;
import com.example.backend.testutil.TestFixtures;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class AuthentificationServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private MockHttpServletResponse response;
    private AuthentificationService service;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), response));
        service = new AuthentificationService(utilisateurRepository, employeRepository, passwordEncoder, jwtService);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginReturnsEmployeeProfileAndAddsHttpOnlyJwtCookie() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setUtilisateur(utilisateur);
        Departement departement = TestFixtures.departement(3L, "RH");
        employe.setDepartement(departement);

        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(utilisateur)).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(3_600_000L);
        when(employeRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(employe));

        AuthUserResponse result = service.login("employee@example.test", "password");

        assertThat(result.getEmail()).isEqualTo("employee@example.test");
        assertThat(result.getRole()).isEqualTo(Role.EMPLOYE);
        assertThat(result.getRedirectUrl()).isEqualTo("/employe/dashboard");
        assertThat(result.getEmployeId()).isEqualTo(10L);
        assertThat(result.getDepartementNom()).isEqualTo("RH");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains(JwtAuthenticationFilter.COOKIE_NAME + "=jwt-token")
                .contains("HttpOnly")
                .contains("SameSite=Lax");
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(utilisateurRepository.findByEmail("missing@example.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("missing@example.test", "password"))
                .isInstanceOf(AuthException.class)
                .hasMessage("Email ou mot de passe incorrect");
    }

    @Test
    void loginRejectsInvalidPassword() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("bad-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> service.login("employee@example.test", "bad-password"))
                .isInstanceOf(AuthException.class)
                .hasMessage("Email ou mot de passe incorrect");
    }

    @Test
    void logoutClearsCookieAndSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("employee@example.test", null, List.of()));

        service.logout();

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains(JwtAuthenticationFilter.COOKIE_NAME + "=")
                .contains("Max-Age=0");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void getCurrentUserReturnsAuthenticatedAdminFallbackProfile() {
        Utilisateur admin = TestFixtures.utilisateur(99L, "admin@example.test", Role.ADMINISTRATEUR);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.test", null, List.of()));
        when(utilisateurRepository.findByEmail("admin@example.test")).thenReturn(Optional.of(admin));
        when(employeRepository.findByUtilisateurId(99L)).thenReturn(Optional.empty());

        AuthUserResponse result = service.getCurrentUser();

        assertThat(result.getEmail()).isEqualTo("admin@example.test");
        assertThat(result.getNom()).isEqualTo("Admin");
        assertThat(result.getRedirectUrl()).isEqualTo("/admin/dashboard");
    }

    @Test
    void getCurrentUserRejectsMissingAuthentication() {
        assertThatThrownBy(() -> service.getCurrentUser())
                .isInstanceOf(AuthException.class)
                .hasMessage("Utilisateur non authentifie");
    }
}
