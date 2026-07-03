package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Admin;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import org.junit.jupiter.api.Test;

class AdminTest {

    @Test
    void shouldStoreAdminFields() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setRole(Role.ADMINISTRATEUR);

        Admin admin = new Admin();
        admin.setIdAdmin(9L);
        admin.setUtilisateur(utilisateur);

        assertThat(admin.getIdAdmin()).isEqualTo(9L);
        assertThat(admin.getUtilisateur()).isSameAs(utilisateur);
        assertThat(admin.getUtilisateur().getRole()).isEqualTo(Role.ADMINISTRATEUR);
    }
}
