package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import org.junit.jupiter.api.Test;

class UtilisateurTest {

    @Test
    void shouldStoreUtilisateurFields() {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setId(1L);
        utilisateur.setEmail("user@example.test");
        utilisateur.setPassword("encoded-password");
        utilisateur.setRole(Role.EMPLOYE);

        assertThat(utilisateur.getId()).isEqualTo(1L);
        assertThat(utilisateur.getEmail()).isEqualTo("user@example.test");
        assertThat(utilisateur.getPassword()).isEqualTo("encoded-password");
        assertThat(utilisateur.getRole()).isEqualTo(Role.EMPLOYE);
    }
}
