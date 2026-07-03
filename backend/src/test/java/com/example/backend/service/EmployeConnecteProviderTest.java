package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.testutil.TestFixtures;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EmployeConnecteProviderTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EmployeRepository employeRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEmployeConnecteReturnsEmployeeLinkedToAuthenticatedUser() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("employee@example.test", null, List.of()));
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(employeRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(employe));

        EmployeConnecteProvider provider = new EmployeConnecteProvider(utilisateurRepository, employeRepository);

        assertThat(provider.getEmployeConnecte()).isSameAs(employe);
    }

    @Test
    void getEmployeConnecteRejectsMissingAuthentication() {
        EmployeConnecteProvider provider = new EmployeConnecteProvider(utilisateurRepository, employeRepository);

        assertThatThrownBy(provider::getEmployeConnecte)
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Utilisateur non authentifie");
    }

    @Test
    void getEmployeConnecteRejectsUserWithoutEmployee() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("employee@example.test", null, List.of()));
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(employeRepository.findByUtilisateurId(1L)).thenReturn(Optional.empty());

        EmployeConnecteProvider provider = new EmployeConnecteProvider(utilisateurRepository, employeRepository);

        assertThatThrownBy(provider::getEmployeConnecte)
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Utilisateur connecte sans Employe lie");
    }
}
