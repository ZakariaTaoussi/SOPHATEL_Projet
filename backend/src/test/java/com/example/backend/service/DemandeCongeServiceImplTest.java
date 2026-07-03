package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.demande.AbsenceStatsResponse;
import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.NatureConge;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.nats.DemandeNotificationEventPublisher;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.impl.DemandeCongeServiceImpl;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.service.interfaces.ISignatureDemandeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandeCongeServiceImplTest {

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private ISoldeCongeService soldeCongeService;

    @Mock
    private ISignatureDemandeService signatureDemandeService;

    @Mock
    private EmployeConnecteProvider employeConnecteProvider;

    @Mock
    private DemandeNotificationEventPublisher notificationEventPublisher;

    private DemandeCongeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DemandeCongeServiceImpl(
                demandeCongeRepository,
                new DemandeCongeMapper(),
                soldeCongeService,
                signatureDemandeService,
                employeConnecteProvider,
                notificationEventPublisher);
    }

    @Test
    void creerBrouillonNormalizesAbsenceWithoutNatureOrDeductedDays() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(employe);
        when(demandeCongeRepository.save(any(DemandeConge.class))).thenAnswer(invocation -> {
            DemandeConge demande = invocation.getArgument(0);
            demande.setId(99L);
            return demande;
        });

        DemandeCongeCreateRequest request = new DemandeCongeCreateRequest();
        request.setDateDebutEmp(LocalDate.of(2026, 7, 6));
        request.setDateFinEmp(LocalDate.of(2026, 7, 7));
        request.setTypeDemande(TypeDemande.ABSENCE);
        request.setNatureConge(NatureConge.ANNUEL);

        service.creerBrouillon(request);

        ArgumentCaptor<DemandeConge> captor = ArgumentCaptor.forClass(DemandeConge.class);
        verify(demandeCongeRepository).save(captor.capture());
        DemandeConge saved = captor.getValue();
        assertThat(saved.getTypeDemande()).isEqualTo(TypeDemande.ABSENCE);
        assertThat(saved.getNatureConge()).isNull();
        assertThat(saved.getJoursDeduits()).isZero();
    }

    @Test
    void directeurGeneralCannotCreateAbsence() {
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(TestFixtures.employe(20L, Role.DIRECTEUR_GENERAL));

        DemandeCongeCreateRequest request = new DemandeCongeCreateRequest();
        request.setDateDebutEmp(LocalDate.of(2026, 7, 6));
        request.setDateFinEmp(LocalDate.of(2026, 7, 7));
        request.setTypeDemande(TypeDemande.ABSENCE);

        assertThatThrownBy(() -> service.creerBrouillon(request))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Ce role ne peut pas declarer une absence");

        verify(demandeCongeRepository, never()).save(any());
    }

    @Test
    void submitEmployeeLeaveDeductsBalanceSignsAndPublishesNotification() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        DemandeConge demande = TestFixtures.demande(
                5L,
                employe,
                TypeDemande.CONGE,
                StatusDemande.BROUILLON,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 8));

        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(employe);
        when(demandeCongeRepository.findByIdAndEmployeId(5L, 10L)).thenReturn(Optional.of(demande));
        when(soldeCongeService.calculerJoursOuvres(demande.getDateDebutEmp(), demande.getDateFinEmp())).thenReturn(3D);
        when(demandeCongeRepository.save(demande)).thenReturn(demande);

        service.submitDemande(5L);

        assertThat(demande.getStatus()).isEqualTo(StatusDemande.VALIDE_EMPLOYE);
        assertThat(demande.getJoursDeduits()).isEqualTo(3D);
        verify(soldeCongeService).deduireSolde(10L, 2026, 3D);
        verify(signatureDemandeService).signerParEmploye(demande, employe);
        verify(notificationEventPublisher).publishAfterCommit(
                demande,
                NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE,
                employe.getUtilisateur().getId());
    }

    @Test
    void getMesAbsencesStatsRejectsInvalidYearBeforeQueryingRepository() {
        assertThatThrownBy(() -> service.getMesAbsencesStats(1800))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Annee invalide");

        verify(demandeCongeRepository, never()).findByEmployeIdAndTypeDemandeAndStatusAndDateDebutEmpBetween(
                any(), any(), any(), any(), any());
    }

    @Test
    void getMesAbsencesStatsGroupsValidatedAbsencesByMonth() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        DemandeConge absence = TestFixtures.demande(
                7L,
                employe,
                TypeDemande.ABSENCE,
                StatusDemande.VALIDE_DG,
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 3));
        absence.setJoursDeduits(2D);

        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(employe);
        when(demandeCongeRepository.findByEmployeIdAndTypeDemandeAndStatusAndDateDebutEmpBetween(
                10L,
                TypeDemande.ABSENCE,
                StatusDemande.VALIDE_DG,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31))).thenReturn(List.of(absence));

        List<AbsenceStatsResponse> stats = service.getMesAbsencesStats(2026);

        assertThat(stats).hasSize(12);
        assertThat(stats.get(2).getMonth()).isEqualTo(3);
        assertThat(stats.get(2).getTotalAbsences()).isEqualTo(1);
        assertThat(stats.get(2).getTotalJoursAbsence()).isEqualTo(2);
    }
}
