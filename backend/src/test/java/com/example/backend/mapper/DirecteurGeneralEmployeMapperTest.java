package com.example.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.dto.directeurgeneral.DirecteurGeneralEmployeResponse;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.enums.Role;
import com.example.backend.testutil.TestFixtures;
import org.junit.jupiter.api.Test;

class DirecteurGeneralEmployeMapperTest {

    @Test
    void toResponseMapsEmployeeForDirecteurGeneralView() {
        Employe employe = TestFixtures.employe(20L, Role.RH);
        Departement departement = TestFixtures.departement(5L, "Finance");
        employe.setDepartement(departement);
        SoldeConge solde = TestFixtures.solde(employe, 2026, 8D, 18D);

        DirecteurGeneralEmployeResponse response = new DirecteurGeneralEmployeMapper().toResponse(employe, solde, 2025);

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getUtilisateurId()).isEqualTo(20L);
        assertThat(response.getEmail()).isEqualTo("user20@example.test");
        assertThat(response.getRole()).isEqualTo("RH");
        assertThat(response.getDepartementId()).isEqualTo(5L);
        assertThat(response.getSoldeActuel()).isEqualTo(8D);
        assertThat(response.getAnneeSolde()).isEqualTo(2026);
    }
}
