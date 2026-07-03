package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.StatutEmploye;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class EmployeTest {

    @Test
    void shouldStoreEmployeFieldsAndDefaultStatus() {
        Utilisateur utilisateur = new Utilisateur();
        Departement departement = new Departement();

        Employe employe = new Employe();
        employe.setIdEmp(10L);
        employe.setMatricule("EMP-010");
        employe.setNom("Nom");
        employe.setPrenom("Prenom");
        employe.setUtilisateur(utilisateur);
        employe.setDepartement(departement);

        assertThat(employe.getIdEmp()).isEqualTo(10L);
        assertThat(employe.getMatricule()).isEqualTo("EMP-010");
        assertThat(employe.getNom()).isEqualTo("Nom");
        assertThat(employe.getPrenom()).isEqualTo("Prenom");
        assertThat(employe.getStatut()).isEqualTo(StatutEmploye.ACTIF);
        assertThat(employe.getUtilisateur()).isSameAs(utilisateur);
        assertThat(employe.getDepartement()).isSameAs(departement);
    }

    @Test
    void prePersistShouldCreateTimestampsWithoutReplacingExistingCreatedAt() {
        Employe employe = new Employe();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 8, 0);
        employe.setCreatedAt(createdAt);

        employe.prePersist();

        assertThat(employe.getCreatedAt()).isEqualTo(createdAt);
        assertThat(employe.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdateShouldRefreshUpdatedAt() {
        Employe employe = new Employe();
        LocalDateTime oldUpdatedAt = LocalDateTime.of(2026, 1, 1, 8, 0);
        employe.setUpdatedAt(oldUpdatedAt);

        employe.preUpdate();

        assertThat(employe.getUpdatedAt()).isAfter(oldUpdatedAt);
    }
}
