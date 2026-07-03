package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.SignatureDemande;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SignatureDemandeTest {

    @Test
    void shouldStoreSignatureDemandeFields() {
        DemandeConge demande = new DemandeConge();
        Employe employe = new Employe();
        Employe responsable = new Employe();
        Employe directeurGeneral = new Employe();
        LocalDateTime signatureEmp = LocalDateTime.of(2026, 7, 1, 9, 0);
        LocalDateTime signatureResp = LocalDateTime.of(2026, 7, 1, 10, 0);
        LocalDateTime signatureDg = LocalDateTime.of(2026, 7, 1, 11, 0);

        SignatureDemande signature = new SignatureDemande();
        signature.setId(1L);
        signature.setDemande(demande);
        signature.setEmploye(employe);
        signature.setResponsable(responsable);
        signature.setDirecteurGeneral(directeurGeneral);
        signature.setDateSignatureEmp(signatureEmp);
        signature.setDateSignatureResp(signatureResp);
        signature.setDateSignatureDg(signatureDg);

        assertThat(signature.getId()).isEqualTo(1L);
        assertThat(signature.getDemande()).isSameAs(demande);
        assertThat(signature.getEmploye()).isSameAs(employe);
        assertThat(signature.getResponsable()).isSameAs(responsable);
        assertThat(signature.getDirecteurGeneral()).isSameAs(directeurGeneral);
        assertThat(signature.getDateSignatureEmp()).isEqualTo(signatureEmp);
        assertThat(signature.getDateSignatureResp()).isEqualTo(signatureResp);
        assertThat(signature.getDateSignatureDg()).isEqualTo(signatureDg);
    }
}
