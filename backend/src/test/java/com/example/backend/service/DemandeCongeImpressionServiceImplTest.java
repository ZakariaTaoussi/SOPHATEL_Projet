package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.SoldeCongeRepository;
import com.example.backend.service.impl.DemandeCongeImpressionServiceImpl;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandeCongeImpressionServiceImplTest {

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private SoldeCongeRepository soldeCongeRepository;

    @Mock
    private EmployeConnecteProvider employeConnecteProvider;

    @Test
    void getDemandePourImpressionReturnsPrintableDemandForOwner() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        Departement departement = TestFixtures.departement(3L, "RH");
        employe.setDepartement(departement);
        DemandeConge demande = printableDemande(employe, StatusDemande.VALIDE_DG);
        SoldeConge solde = TestFixtures.solde(employe, 2026, 11D, 18D);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(employe);
        when(soldeCongeRepository.findByEmployeIdAndAnnee(10L, 2026)).thenReturn(Optional.of(solde));

        DemandeCongeImpressionResponse response = service().getDemandePourImpression(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNom()).isEqualTo("Nom");
        assertThat(response.getFonction()).isEqualTo("EMPLOYE");
        assertThat(response.getService()).isEqualTo("RH");
        assertThat(response.getReliquatConge()).isEqualTo(11D);
        assertThat(response.getStatutDemandeur()).isEqualTo("VALIDE_EMPLOYE");
        assertThat(response.getStatutResponsable()).isEqualTo("VALIDE_RESPONSABLE");
        assertThat(response.getStatutDirecteurGeneral()).isEqualTo("VALIDE_DG");
    }

    @Test
    void getDemandePourImpressionRejectsNonPrintableStatus() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        DemandeConge demande = printableDemande(employe, StatusDemande.BROUILLON);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));

        assertThatThrownBy(() -> service().getDemandePourImpression(1L))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Cette demande ne peut pas encore etre imprimee.");
    }

    @Test
    void getDemandePourImpressionRejectsEmployeeFromAnotherDepartment() {
        Employe demandeur = TestFixtures.employe(10L, Role.EMPLOYE);
        demandeur.setDepartement(TestFixtures.departement(3L, "RH"));
        Employe connected = TestFixtures.employe(20L, Role.EMPLOYE);
        connected.setDepartement(TestFixtures.departement(4L, "Finance"));
        DemandeConge demande = printableDemande(demandeur, StatusDemande.VALIDE_DG);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(connected);

        assertThatThrownBy(() -> service().getDemandePourImpression(1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Vous n'avez pas le droit d'imprimer cette demande.");
    }

    @Test
    void responsableCanPrintTeamDemand() {
        Departement departement = TestFixtures.departement(3L, "RH");
        Employe demandeur = TestFixtures.employe(10L, Role.EMPLOYE);
        demandeur.setDepartement(departement);
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setDepartement(departement);
        DemandeConge demande = printableDemande(demandeur, StatusDemande.REFUSE_DG);
        when(demandeCongeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(employeConnecteProvider.getEmployeConnecte()).thenReturn(responsable);
        when(soldeCongeRepository.findByEmployeIdAndAnnee(10L, 2026)).thenReturn(Optional.empty());

        DemandeCongeImpressionResponse response = service().getDemandePourImpression(1L);

        assertThat(response.getStatutDirecteurGeneral()).isEqualTo("REFUSE_DG");
        assertThat(response.getReliquatConge()).isNull();
    }

    private DemandeCongeImpressionServiceImpl service() {
        return new DemandeCongeImpressionServiceImpl(
                demandeCongeRepository,
                soldeCongeRepository,
                employeConnecteProvider);
    }

    private DemandeConge printableDemande(Employe employe, StatusDemande status) {
        DemandeConge demande = TestFixtures.demande(
                1L,
                employe,
                TypeDemande.CONGE,
                status,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        demande.setDateDebutDg(LocalDate.of(2026, 7, 6));
        demande.setDateFinDg(LocalDate.of(2026, 7, 7));
        demande.setJoursDeduits(2D);
        return demande;
    }
}
