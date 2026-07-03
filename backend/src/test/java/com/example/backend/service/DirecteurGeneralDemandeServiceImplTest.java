package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.demande.DirecteurGeneralDemandeResponse;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.nats.DemandeNotificationEventPublisher;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.impl.DirecteurGeneralDemandeServiceImpl;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.service.interfaces.ISignatureDemandeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirecteurGeneralDemandeServiceImplTest {

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

    @Test
    void validerDemandeParDgUsesResponsableDatesAndDeductsFinalBalance() {
        Employe dg = TestFixtures.employe(30L, Role.DIRECTEUR_GENERAL);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        DemandeConge demande = TestFixtures.demande(
                1L,
                employe,
                TypeDemande.CONGE,
                StatusDemande.VALIDE_RESPONSABLE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        demande.setDateDebutResp(LocalDate.of(2026, 7, 8));
        demande.setDateFinResp(LocalDate.of(2026, 7, 9));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(dg);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(soldeCongeService.calculerJoursOuvres(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 9))).thenReturn(2D);
        when(demandeCongeRepository.save(demande)).thenReturn(demande);

        DirecteurGeneralDemandeResponse response = service().validerDemandeParDg(1L, null);

        assertThat(response.getStatus()).isEqualTo(StatusDemande.VALIDE_DG);
        assertThat(demande.getDateDebutDg()).isEqualTo(LocalDate.of(2026, 7, 8));
        assertThat(demande.getJoursDeduits()).isEqualTo(2D);
        verify(soldeCongeService).deduireSolde(10L, 2026, 2D);
        verify(signatureDemandeService).signerParDg(demande, dg);
        verify(notificationEventPublisher).publishAfterCommit(
                demande,
                NotificationType.DEMANDE_VALIDATED_BY_DG,
                dg.getUtilisateur().getId());
    }

    @Test
    void validerDemandeParDgRejectsDemandWithoutResponsableDates() {
        Employe dg = TestFixtures.employe(30L, Role.DIRECTEUR_GENERAL);
        DemandeConge demande = TestFixtures.demande(
                1L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.CONGE,
                StatusDemande.VALIDE_RESPONSABLE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(dg);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service().validerDemandeParDg(1L, null))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Les dates responsable doivent exister avant validation DG");
    }

    @Test
    void refuserDemandeParDgSetsRefusedStatusAndPublishesEvent() {
        Employe dg = TestFixtures.employe(30L, Role.DIRECTEUR_GENERAL);
        DemandeConge demande = TestFixtures.demande(
                1L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.ABSENCE,
                StatusDemande.VALIDE_RESPONSABLE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(dg);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(demandeCongeRepository.save(demande)).thenReturn(demande);

        DirecteurGeneralDemandeResponse response = service().refuserDemandeParDg(1L);

        assertThat(response.getStatus()).isEqualTo(StatusDemande.REFUSE_DG);
        verify(notificationEventPublisher).publishAfterCommit(
                demande,
                NotificationType.DEMANDE_REFUSED_BY_DG,
                dg.getUtilisateur().getId());
    }

    private DirecteurGeneralDemandeServiceImpl service() {
        return new DirecteurGeneralDemandeServiceImpl(
                demandeCongeRepository,
                new DemandeCongeMapper(),
                soldeCongeService,
                signatureDemandeService,
                employeConnecteProvider,
                notificationEventPublisher);
    }
}
