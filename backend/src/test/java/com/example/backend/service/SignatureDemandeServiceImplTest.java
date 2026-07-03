package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.SignatureDemande;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.SignatureDemandeRepository;
import com.example.backend.service.impl.SignatureDemandeServiceImpl;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SignatureDemandeServiceImplTest {

    @Mock
    private SignatureDemandeRepository signatureDemandeRepository;

    @Test
    void signerParEmployeCreatesSignatureWhenMissing() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        DemandeConge demande = TestFixtures.demande(
                1L, employe, TypeDemande.CONGE, StatusDemande.VALIDE_EMPLOYE,
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 7));
        when(signatureDemandeRepository.findByDemandeId(1L)).thenReturn(Optional.empty());

        new SignatureDemandeServiceImpl(signatureDemandeRepository).signerParEmploye(demande, employe);

        ArgumentCaptor<SignatureDemande> captor = ArgumentCaptor.forClass(SignatureDemande.class);
        verify(signatureDemandeRepository).save(captor.capture());
        assertThat(captor.getValue().getDemande()).isSameAs(demande);
        assertThat(captor.getValue().getEmploye()).isSameAs(employe);
        assertThat(captor.getValue().getDateSignatureEmp()).isNotNull();
    }

    @Test
    void signerParResponsableUpdatesExistingSignature() {
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        DemandeConge demande = TestFixtures.demande(
                2L, responsable, TypeDemande.CONGE, StatusDemande.VALIDE_RESPONSABLE,
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 7));
        SignatureDemande existing = new SignatureDemande();
        when(signatureDemandeRepository.findByDemandeId(2L)).thenReturn(Optional.of(existing));

        new SignatureDemandeServiceImpl(signatureDemandeRepository).signerParResponsable(demande, responsable);

        assertThat(existing.getDemande()).isSameAs(demande);
        assertThat(existing.getResponsable()).isSameAs(responsable);
        assertThat(existing.getDateSignatureResp()).isNotNull();
        verify(signatureDemandeRepository).save(existing);
    }

    @Test
    void signerParDirecteurGeneralDelegatesToDgSignature() {
        Employe dg = TestFixtures.employe(30L, Role.DIRECTEUR_GENERAL);
        DemandeConge demande = TestFixtures.demande(
                3L, dg, TypeDemande.CONGE, StatusDemande.VALIDE_DG,
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 7));
        when(signatureDemandeRepository.findByDemandeId(3L)).thenReturn(Optional.empty());

        new SignatureDemandeServiceImpl(signatureDemandeRepository).signerParDirecteurGeneral(demande, dg);

        ArgumentCaptor<SignatureDemande> captor = ArgumentCaptor.forClass(SignatureDemande.class);
        verify(signatureDemandeRepository).save(captor.capture());
        assertThat(captor.getValue().getDirecteurGeneral()).isSameAs(dg);
        assertThat(captor.getValue().getDateSignatureDg()).isNotNull();
    }
}
