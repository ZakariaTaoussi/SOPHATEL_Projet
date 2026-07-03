package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.NatureConge;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DemandeCongeTest {

    @Test
    void shouldStoreDemandeCongeFields() {
        Employe employe = new Employe();

        DemandeConge demande = new DemandeConge();
        demande.setId(20L);
        demande.setEmploye(employe);
        demande.setDateDebutEmp(LocalDate.of(2026, 7, 6));
        demande.setDateFinEmp(LocalDate.of(2026, 7, 7));
        demande.setDateDebutResp(LocalDate.of(2026, 7, 8));
        demande.setDateFinResp(LocalDate.of(2026, 7, 9));
        demande.setDateDebutDg(LocalDate.of(2026, 7, 10));
        demande.setDateFinDg(LocalDate.of(2026, 7, 11));
        demande.setTypeDemande(TypeDemande.CONGE);
        demande.setNatureConge(NatureConge.ANNUEL);
        demande.setStatus(StatusDemande.VALIDE_EMPLOYE);
        demande.setJoursDeduits(2D);

        assertThat(demande.getId()).isEqualTo(20L);
        assertThat(demande.getEmploye()).isSameAs(employe);
        assertThat(demande.getDateDebutEmp()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(demande.getDateFinEmp()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(demande.getDateDebutResp()).isEqualTo(LocalDate.of(2026, 7, 8));
        assertThat(demande.getDateFinResp()).isEqualTo(LocalDate.of(2026, 7, 9));
        assertThat(demande.getDateDebutDg()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(demande.getDateFinDg()).isEqualTo(LocalDate.of(2026, 7, 11));
        assertThat(demande.getTypeDemande()).isEqualTo(TypeDemande.CONGE);
        assertThat(demande.getNatureConge()).isEqualTo(NatureConge.ANNUEL);
        assertThat(demande.getStatus()).isEqualTo(StatusDemande.VALIDE_EMPLOYE);
        assertThat(demande.getJoursDeduits()).isEqualTo(2D);
    }

    @Test
    void prePersistShouldSetTimestampsAndDefaultValues() {
        DemandeConge demande = new DemandeConge();
        demande.setStatus(null);
        demande.setJoursDeduits(null);

        ReflectionTestUtils.invokeMethod(demande, "prePersist");

        assertThat(demande.getCreatedAt()).isNotNull();
        assertThat(demande.getUpdatedAt()).isNotNull();
        assertThat(demande.getStatus()).isEqualTo(StatusDemande.BROUILLON);
        assertThat(demande.getJoursDeduits()).isEqualTo(0D);
    }

    @Test
    void preUpdateShouldRefreshUpdatedAtAndDefaultJoursDeduits() {
        DemandeConge demande = new DemandeConge();
        LocalDateTime oldUpdatedAt = LocalDateTime.of(2026, 1, 1, 8, 0);
        demande.setUpdatedAt(oldUpdatedAt);
        demande.setJoursDeduits(null);

        ReflectionTestUtils.invokeMethod(demande, "preUpdate");

        assertThat(demande.getUpdatedAt()).isAfter(oldUpdatedAt);
        assertThat(demande.getJoursDeduits()).isEqualTo(0D);
    }
}
