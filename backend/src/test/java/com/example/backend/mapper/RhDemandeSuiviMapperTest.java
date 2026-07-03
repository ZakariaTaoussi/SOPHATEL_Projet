package com.example.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.dto.rh.RhDemandeSuiviResponse;
import com.example.backend.model.Departement;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RhDemandeSuiviMapperTest {

    @Test
    void toResponseMapsDemandeForRhFollowUp() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setDepartement(TestFixtures.departement(4L, "Informatique"));
        DemandeConge demande = TestFixtures.demande(
                15L,
                employe,
                TypeDemande.CONGE,
                StatusDemande.VALIDE_DG,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        demande.setDateDebutDg(LocalDate.of(2026, 7, 6));
        demande.setDateFinDg(LocalDate.of(2026, 7, 7));
        demande.setJoursDeduits(2D);

        RhDemandeSuiviResponse response = new RhDemandeSuiviMapper().toResponse(demande);

        assertThat(response.id()).isEqualTo(15L);
        assertThat(response.reference()).isEqualTo("DEM-15");
        assertThat(response.employeId()).isEqualTo(10L);
        assertThat(response.employeNomComplet()).isEqualTo("Prenom Nom");
        assertThat(response.departementNom()).isEqualTo("Informatique");
        assertThat(response.typeDemande()).isEqualTo("CONGE");
        assertThat(response.status()).isEqualTo("VALIDE_DG");
        assertThat(response.joursDeduits()).isEqualTo(2D);
    }

    @Test
    void toResponseHandlesNullDemande() {
        RhDemandeSuiviResponse response = new RhDemandeSuiviMapper().toResponse(null);

        assertThat(response.id()).isNull();
        assertThat(response.joursDeduits()).isZero();
    }
}
