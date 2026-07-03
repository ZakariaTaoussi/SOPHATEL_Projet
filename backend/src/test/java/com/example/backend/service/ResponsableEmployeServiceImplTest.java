package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.responsable.ResponsableEmployeResponse;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.mapper.ResponsableEmployeMapper;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.ResponsableEmployeServiceImpl;
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
class ResponsableEmployeServiceImplTest {

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
    void getMesEmployesReturnsTeamPageWithDefaultSize() {
        Departement departement = TestFixtures.departement(3L, "RH");
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setDepartement(departement);
        Employe employee = TestFixtures.employe(10L, Role.EMPLOYE);
        employee.setDepartement(departement);
        int currentYear = LocalDate.now().getYear();
        authenticateAs("responsable@example.test");
        responsable.getUtilisateur().setEmail("responsable@example.test");
        when(utilisateurRepository.findByEmail("responsable@example.test")).thenReturn(Optional.of(responsable.getUtilisateur()));
        when(employeRepository.findByUtilisateurId(20L)).thenReturn(Optional.of(responsable));
        when(employeRepository.findEmployesEquipeResponsable(
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.eq(20L),
                any(Set.class),
                org.mockito.ArgumentMatchers.eq(PageRequest.of(0, 4))))
                .thenReturn(new PageImpl<>(List.of(employee), PageRequest.of(0, 4), 1));
        when(soldeCongeService.getOrCreateSolde(10L, currentYear))
                .thenReturn(TestFixtures.solde(employee, currentYear, 12D, 18D));

        PageResponse<ResponsableEmployeResponse> response = service().getMesEmployes(0, 0);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).getId()).isEqualTo(10L);
        assertThat(response.size()).isEqualTo(4);
    }

    @Test
    void getMesEmployesRejectsTooLargePageSize() {
        Departement departement = TestFixtures.departement(3L, "RH");
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setDepartement(departement);
        authenticateAs("responsable@example.test");
        responsable.getUtilisateur().setEmail("responsable@example.test");
        when(utilisateurRepository.findByEmail("responsable@example.test")).thenReturn(Optional.of(responsable.getUtilisateur()));
        when(employeRepository.findByUtilisateurId(20L)).thenReturn(Optional.of(responsable));

        assertThatThrownBy(() -> service().getMesEmployes(0, 99))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getMesEmployesRejectsNonResponsableUser() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        authenticateAs("employee@example.test");
        employe.getUtilisateur().setEmail("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(employe.getUtilisateur()));

        assertThatThrownBy(() -> service().getMesEmployes(0, 4))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getEmployeDeMonDepartementRejectsEmployeeOutsideDepartment() {
        Departement responsableDept = TestFixtures.departement(3L, "RH");
        Departement otherDept = TestFixtures.departement(4L, "Finance");
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setDepartement(responsableDept);
        Employe employee = TestFixtures.employe(10L, Role.EMPLOYE);
        employee.setDepartement(otherDept);
        authenticateAs("responsable@example.test");
        responsable.getUtilisateur().setEmail("responsable@example.test");
        when(utilisateurRepository.findByEmail("responsable@example.test")).thenReturn(Optional.of(responsable.getUtilisateur()));
        when(employeRepository.findByUtilisateurId(20L)).thenReturn(Optional.of(responsable));
        when(employeRepository.findById(10L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> service().getEmployeDeMonDepartement(10L))
                .isInstanceOf(ForbiddenException.class);
    }

    private ResponsableEmployeServiceImpl service() {
        return new ResponsableEmployeServiceImpl(
                utilisateurRepository,
                employeRepository,
                soldeCongeService,
                new ResponsableEmployeMapper());
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
