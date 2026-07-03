package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.enums.Role;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void shouldExposeExpectedValues() {
        assertThat(Role.values())
                .containsExactly(
                        Role.EMPLOYE,
                        Role.RH,
                        Role.RESPONSABLE,
                        Role.ADMINISTRATEUR,
                        Role.DIRECTEUR_GENERAL);
    }
}
