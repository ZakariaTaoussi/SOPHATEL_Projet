package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.admin.CreateDepartementRequest;
import com.example.backend.dto.admin.DepartementResponse;
import com.example.backend.dto.admin.UpdateDepartementRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceAlreadyExistsException;
import com.example.backend.exception.ResourceConflictException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.service.impl.GestionDepartementService;
import com.example.backend.testutil.TestFixtures;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestionDepartementServiceTest {

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Test
    void creerDepartementTrimsNameAndPersists() {
        CreateDepartementRequest request = new CreateDepartementRequest();
        request.setNom("  Informatique  ");
        when(departementRepository.existsByNomIgnoreCase("Informatique")).thenReturn(false);
        when(departementRepository.save(any(Departement.class))).thenAnswer(invocation -> {
            Departement departement = invocation.getArgument(0);
            departement.setId(1L);
            return departement;
        });

        DepartementResponse response = service().creerDepartement(request);

        ArgumentCaptor<Departement> captor = ArgumentCaptor.forClass(Departement.class);
        verify(departementRepository).save(captor.capture());
        assertThat(captor.getValue().getNom()).isEqualTo("Informatique");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nom()).isEqualTo("Informatique");
    }

    @Test
    void creerDepartementRejectsBlankName() {
        CreateDepartementRequest request = new CreateDepartementRequest();
        request.setNom(" ");

        assertThatThrownBy(() -> service().creerDepartement(request))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Le nom du departement est obligatoire");
    }

    @Test
    void creerDepartementRejectsDuplicateName() {
        CreateDepartementRequest request = new CreateDepartementRequest();
        request.setNom("RH");
        when(departementRepository.existsByNomIgnoreCase("RH")).thenReturn(true);

        assertThatThrownBy(() -> service().creerDepartement(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Un departement avec ce nom existe deja");

        verify(departementRepository, never()).save(any());
    }

    @Test
    void modifierDepartementUpdatesExistingDepartment() {
        Departement departement = TestFixtures.departement(1L, "RH");
        UpdateDepartementRequest request = new UpdateDepartementRequest();
        request.setNom("Finance");
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(departementRepository.existsByNomIgnoreCaseAndIdNot("Finance", 1L)).thenReturn(false);
        when(departementRepository.save(departement)).thenReturn(departement);

        DepartementResponse response = service().modifierDepartement(1L, request);

        assertThat(response.nom()).isEqualTo("Finance");
        verify(departementRepository).save(departement);
    }

    @Test
    void supprimerDepartementRejectsDepartmentWithEmployees() {
        Departement departement = TestFixtures.departement(1L, "RH");
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(employeRepository.existsByDepartementId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service().supprimerDepartement(1L))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Impossible de supprimer un departement contenant des employes");

        verify(departementRepository, never()).delete(any());
    }

    @Test
    void consulterDepartementsMapsResponsableAndEmployeeCount() {
        Departement departement = TestFixtures.departement(1L, "RH");
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setPrenom("Sara");
        responsable.setNom("Manager");
        departement.setResponsable(responsable);
        departement.setEmployes(List.of(responsable, TestFixtures.employe(21L, Role.EMPLOYE)));
        when(departementRepository.findAll()).thenReturn(List.of(departement));

        List<DepartementResponse> responses = service().consulterDepartements();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).responsableId()).isEqualTo(20L);
        assertThat(responses.get(0).responsableNomComplet()).isEqualTo("Sara Manager");
        assertThat(responses.get(0).nombreEmployes()).isEqualTo(2);
    }

    private GestionDepartementService service() {
        return new GestionDepartementService(departementRepository, employeRepository);
    }
}
