package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.directeurgeneral.DirecteurGeneralEmployeResponse;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.mapper.DirecteurGeneralEmployeMapper;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.DirecteurGeneralEmployeServiceImpl;
import com.example.backend.service.interfaces.ISoldeCongeService;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DirecteurGeneralEmployeServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private ISoldeCongeService soldeCongeService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEmployesNormalizesSearchRoleAndPagination() {
        Employe dg = TestFixtures.employe(1L, Role.DIRECTEUR_GENERAL);
        dg.getUtilisateur().setEmail("dg@example.test");
        Employe employe = TestFixtures.employe(10L, Role.RH);
        int currentYear = LocalDate.now().getYear();
        authenticateAs("dg@example.test");
        when(utilisateurRepository.findByEmail("dg@example.test")).thenReturn(Optional.of(dg.getUtilisateur()));
        when(employeRepository.findEmployesForDirecteurGeneral(
                any(Set.class),
                org.mockito.ArgumentMatchers.eq(Role.RH),
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq("%ali%"),
                org.mockito.ArgumentMatchers.eq(PageRequest.of(0, 4))))
                .thenReturn(new PageImpl<>(List.of(employe), PageRequest.of(0, 4), 1));
        when(soldeCongeService.getOrCreateSolde(10L, currentYear))
                .thenReturn(TestFixtures.solde(employe, currentYear, 9D, 18D));

        PageResponse<DirecteurGeneralEmployeResponse> response =
                service().getEmployes(0, 0, "  Ali  ", "rh", 3L);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).getId()).isEqualTo(10L);
        assertThat(response.size()).isEqualTo(4);
    }

    @Test
    void getEmployesRejectsInvalidPaginationAndRole() {
        Employe dg = TestFixtures.employe(1L, Role.DIRECTEUR_GENERAL);
        dg.getUtilisateur().setEmail("dg@example.test");
        authenticateAs("dg@example.test");
        when(utilisateurRepository.findByEmail("dg@example.test")).thenReturn(Optional.of(dg.getUtilisateur()));

        assertThatThrownBy(() -> service().getEmployes(-1, 4, null, null, null))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service().getEmployes(0, 4, null, "ADMINISTRATEUR", null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getEmployesRejectsNonDirecteurGeneral() {
        Employe rh = TestFixtures.employe(2L, Role.RH);
        rh.getUtilisateur().setEmail("rh@example.test");
        authenticateAs("rh@example.test");
        when(utilisateurRepository.findByEmail("rh@example.test")).thenReturn(Optional.of(rh.getUtilisateur()));

        assertThatThrownBy(() -> service().getEmployes(0, 4, null, null, null))
                .isInstanceOf(ForbiddenException.class);
    }

    private DirecteurGeneralEmployeServiceImpl service() {
        return new DirecteurGeneralEmployeServiceImpl(
                utilisateurRepository,
                employeRepository,
                soldeCongeService,
                new DirecteurGeneralEmployeMapper());
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
