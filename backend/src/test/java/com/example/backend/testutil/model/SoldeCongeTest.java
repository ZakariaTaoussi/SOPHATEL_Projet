package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SoldeCongeTest {

    @Test
    void shouldStoreSoldeCongeFields() {
        Employe employe = new Employe();

        SoldeConge solde = new SoldeConge();
        solde.setId(1L);
        solde.setEmploye(employe);
        solde.setAnnee(2026);
        solde.setSoldeActuel(12D);
        solde.setSoldeTotal(18D);

        assertThat(solde.getId()).isEqualTo(1L);
        assertThat(solde.getEmploye()).isSameAs(employe);
        assertThat(solde.getAnnee()).isEqualTo(2026);
        assertThat(solde.getSoldeActuel()).isEqualTo(12D);
        assertThat(solde.getSoldeTotal()).isEqualTo(18D);
    }

    @Test
    void prePersistShouldApplyDefaultSoldeValues() {
        SoldeConge solde = new SoldeConge();

        ReflectionTestUtils.invokeMethod(solde, "prePersist");

        assertThat(solde.getSoldeActuel()).isEqualTo(18D);
        assertThat(solde.getSoldeTotal()).isEqualTo(18D);
    }
}
