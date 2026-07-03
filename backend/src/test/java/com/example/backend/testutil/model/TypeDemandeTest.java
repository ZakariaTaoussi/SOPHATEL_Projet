package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.enums.TypeDemande;
import org.junit.jupiter.api.Test;

class TypeDemandeTest {

    @Test
    void shouldExposeExpectedValues() {
        assertThat(TypeDemande.values())
                .containsExactly(
                        TypeDemande.CONGE,
                        TypeDemande.ABSENCE);
    }
}
