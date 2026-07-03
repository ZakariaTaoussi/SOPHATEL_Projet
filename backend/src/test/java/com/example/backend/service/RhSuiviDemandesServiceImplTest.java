package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.dto.rh.RhDepartementResponse;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.mapper.RhDemandeSuiviMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.service.impl.RhSuiviDemandesServiceImpl;
import com.example.backend.service.interfaces.IDemandeCongeImpressionService;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RhSuiviDemandesServiceImplTest {

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private EmployeConnecteProvider employeConnecteProvider;

    @Mock
    private IDemandeCongeImpressionService impressionService;

    @Test
    void getDepartementsRequiresRhAndMapsDepartments() {
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(TestFixtures.employe(10L, Role.RH));
        when(departementRepository.findAll()).thenReturn(List.of(TestFixtures.departement(1L, "RH")));

        List<RhDepartementResponse> response = service().getDepartements();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(0).nom()).isEqualTo("RH");
    }

    @Test
    void getDepartementsRejectsNonRhUser() {
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(TestFixtures.employe(10L, Role.EMPLOYE));

        assertThatThrownBy(() -> service().getDepartements())
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Acces reserve au service RH.");
    }

    @Test
    void imprimerCongeValideDgValidatesTypeAndStatusBeforeDelegating() {
        DemandeConge demande = TestFixtures.demande(
                1L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.CONGE,
                StatusDemande.VALIDE_DG,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        DemandeCongeImpressionResponse expected = new DemandeCongeImpressionResponse(
                1L,
                "Nom",
                "Prenom",
                "EMPLOYE",
                "RH",
                TypeDemande.CONGE,
                null,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7),
                2D,
                2026,
                2026,
                10D,
                StatusDemande.VALIDE_DG,
                "VALIDE_EMPLOYE",
                "VALIDE_RESPONSABLE",
                "VALIDE_DG",
                LocalDateTime.now(),
                LocalDateTime.now());
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(TestFixtures.employe(20L, Role.RH));
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(impressionService.getDemandePourImpression(1L)).thenReturn(expected);

        assertThat(service().imprimerCongeValideDg(1L)).isSameAs(expected);
    }

    @Test
    void imprimerCongeValideDgRejectsWrongType() {
        DemandeConge demande = TestFixtures.demande(
                1L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.ABSENCE,
                StatusDemande.VALIDE_DG,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(TestFixtures.employe(20L, Role.RH));
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service().imprimerCongeValideDg(1L))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Type de demande invalide pour cette impression");
    }

    private RhSuiviDemandesServiceImpl service() {
        return new RhSuiviDemandesServiceImpl(
                demandeCongeRepository,
                departementRepository,
                employeConnecteProvider,
                impressionService,
                new RhDemandeSuiviMapper());
    }
}
