package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.admin.CreateEmployeRequest;
import com.example.backend.dto.admin.EmployeResponse;
import com.example.backend.dto.admin.PageResponse;
import com.example.backend.dto.admin.UpdateEmployeRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceAlreadyExistsException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.GestionEmployeService;
import com.example.backend.testutil.TestFixtures;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class GestionEmployeServiceTest {

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void creerEmployeCreatesUserAndEmployeeAndSynchronizesResponsableDepartment() {
        Departement departement = TestFixtures.departement(3L, "RH");
        CreateEmployeRequest request = createRequest(Role.RESPONSABLE);
        request.setDepartementId(3L);
        when(utilisateurRepository.existsByEmail("employee@example.test")).thenReturn(false);
        when(employeRepository.existsByMatricule("EMP-001")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur utilisateur = invocation.getArgument(0);
            utilisateur.setId(7L);
            return utilisateur;
        });
        when(departementRepository.findById(3L)).thenReturn(Optional.of(departement));
        when(employeRepository.save(any(Employe.class))).thenAnswer(invocation -> {
            Employe employe = invocation.getArgument(0);
            employe.setIdEmp(10L);
            return employe;
        });

        EmployeResponse response = service().creerEmploye(request);

        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("employee@example.test");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.role()).isEqualTo(Role.RESPONSABLE);
        assertThat(departement.getResponsable().getIdEmp()).isEqualTo(10L);
        verify(departementRepository).clearResponsable(10L);
        verify(departementRepository).save(departement);
    }

    @Test
    void creerEmployeRejectsDuplicateEmail() {
        CreateEmployeRequest request = createRequest(Role.EMPLOYE);
        when(utilisateurRepository.existsByEmail("employee@example.test")).thenReturn(true);

        assertThatThrownBy(() -> service().creerEmploye(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Email deja utilise");

        verify(employeRepository, never()).save(any());
    }

    @Test
    void creerEmployeRejectsMissingPassword() {
        CreateEmployeRequest request = createRequest(Role.EMPLOYE);
        request.setPassword(" ");

        assertThatThrownBy(() -> service().creerEmploye(request))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Le mot de passe est obligatoire");
    }

    @Test
    void modifierEmployeKeepsPasswordWhenNotProvided() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        Departement departement = TestFixtures.departement(4L, "Finance");
        UpdateEmployeRequest request = updateRequest(Role.RH);
        request.setPassword(" ");
        request.setDepartementId(4L);
        when(employeRepository.findById(10L)).thenReturn(Optional.of(employe));
        when(utilisateurRepository.existsByEmailAndIdNot("updated@example.test", 10L)).thenReturn(false);
        when(employeRepository.existsByMatriculeAndIdEmpNot("EMP-002", 10L)).thenReturn(false);
        when(departementRepository.findById(4L)).thenReturn(Optional.of(departement));
        when(employeRepository.save(employe)).thenReturn(employe);

        EmployeResponse response = service().modifierEmploye(10L, request);

        assertThat(response.email()).isEqualTo("updated@example.test");
        assertThat(response.role()).isEqualTo(Role.RH);
        assertThat(response.departementNom()).isEqualTo("Finance");
        assertThat(employe.getUtilisateur().getPassword()).isEqualTo("encoded-password");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void consulterEmployesUsesSearchWhenProvided() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        when(employeRepository.search("Nom", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(java.util.List.of(employe), PageRequest.of(0, 5), 1));

        PageResponse<EmployeResponse> response = service().consulterEmployes("  Nom  ", PageRequest.of(0, 5));

        assertThat(response.content()).hasSize(1);
        assertThat(response.currentPage()).isZero();
        assertThat(response.totalElements()).isEqualTo(1);
    }

    private GestionEmployeService service() {
        return new GestionEmployeService(employeRepository, utilisateurRepository, departementRepository, passwordEncoder);
    }

    private CreateEmployeRequest createRequest(Role role) {
        CreateEmployeRequest request = new CreateEmployeRequest();
        request.setMatricule("EMP-001");
        request.setNom("Nom");
        request.setPrenom("Prenom");
        request.setEmail("employee@example.test");
        request.setPassword("password");
        request.setRole(role);
        return request;
    }

    private UpdateEmployeRequest updateRequest(Role role) {
        UpdateEmployeRequest request = new UpdateEmployeRequest();
        request.setMatricule("EMP-002");
        request.setNom("UpdatedNom");
        request.setPrenom("UpdatedPrenom");
        request.setEmail("updated@example.test");
        request.setRole(role);
        return request;
    }
}
