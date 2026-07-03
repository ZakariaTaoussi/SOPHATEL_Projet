package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.enums.NatureConge;
import org.junit.jupiter.api.Test;

class NatureCongeTest {

    @Test
    void shouldExposeExpectedValues() {
        assertThat(NatureConge.values())
                .containsExactly(
                        NatureConge.ANNUEL,
                        NatureConge.MALADIE,
                        NatureConge.MATERNITE,
                        NatureConge.MISE_EN_DISPONIBILITE);
    }
}
