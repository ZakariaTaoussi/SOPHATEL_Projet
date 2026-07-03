package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.nats.DemandeNotificationEventPublisher;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.service.impl.ResponsableDemandeServiceImpl;
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
class ResponsableDemandeServiceImplTest {

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
    void validerDemandeParResponsableUsesEmployeeDatesWhenRequestIsNull() {
        Departement departement = TestFixtures.departement(3L, "RH");
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setDepartement(departement);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setDepartement(departement);
        DemandeConge demande = TestFixtures.demande(
                1L,
                employe,
                TypeDemande.CONGE,
                StatusDemande.VALIDE_EMPLOYE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(responsable);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(soldeCongeService.calculerJoursOuvres(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 7))).thenReturn(2D);
        when(demandeCongeRepository.save(demande)).thenReturn(demande);

        ResponsableDemandeResponse response = service().validerDemandeParResponsable(1L, null);

        assertThat(response.getStatus()).isEqualTo(StatusDemande.VALIDE_RESPONSABLE);
        assertThat(demande.getDateDebutResp()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(demande.getJoursDeduits()).isEqualTo(2D);
        verify(soldeCongeService).deduireSolde(10L, 2026, 2D);
        verify(signatureDemandeService).signerParResponsable(demande, responsable);
        verify(notificationEventPublisher).publishAfterCommit(
                demande,
                NotificationType.DEMANDE_VALIDATED_BY_RESPONSABLE,
                responsable.getUtilisateur().getId());
    }

    @Test
    void validerDemandeParResponsableRejectsNonResponsableUser() {
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(TestFixtures.employe(10L, Role.EMPLOYE));

        assertThatThrownBy(() -> service().validerDemandeParResponsable(1L, null))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("L'utilisateur connecte n'a pas le role RESPONSABLE");
    }

    @Test
    void refuserDemandeParResponsableSetsRefusedStatusAndPublishesEvent() {
        Departement departement = TestFixtures.departement(3L, "RH");
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setDepartement(departement);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setDepartement(departement);
        DemandeConge demande = TestFixtures.demande(
                1L,
                employe,
                TypeDemande.ABSENCE,
                StatusDemande.VALIDE_EMPLOYE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        demande.setJoursDeduits(2D);
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(responsable);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(demandeCongeRepository.save(demande)).thenReturn(demande);

        ResponsableDemandeResponse response = service().refuserDemandeParResponsable(1L);

        assertThat(response.getStatus()).isEqualTo(StatusDemande.REFUSE_RESPONSABLE);
        assertThat(demande.getJoursDeduits()).isZero();
        verify(notificationEventPublisher).publishAfterCommit(
                demande,
                NotificationType.DEMANDE_REFUSED_BY_RESPONSABLE,
                responsable.getUtilisateur().getId());
    }

    private ResponsableDemandeServiceImpl service() {
        return new ResponsableDemandeServiceImpl(
                demandeCongeRepository,
                new DemandeCongeMapper(),
                soldeCongeService,
                signatureDemandeService,
                employeConnecteProvider,
                notificationEventPublisher);
    }
}
