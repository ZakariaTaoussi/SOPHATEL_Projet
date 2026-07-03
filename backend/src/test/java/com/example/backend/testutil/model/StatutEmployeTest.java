package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.enums.StatutEmploye;
import org.junit.jupiter.api.Test;

class StatutEmployeTest {

    @Test
    void shouldExposeExpectedValues() {
        assertThat(StatutEmploye.values())
                .containsExactly(
                        StatutEmploye.ACTIF,
                        StatutEmploye.INACTIF);
    }
}
