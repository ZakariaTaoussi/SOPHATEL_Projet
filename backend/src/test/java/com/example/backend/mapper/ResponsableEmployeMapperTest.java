package com.example.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.dto.responsable.ResponsableEmployeResponse;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.enums.Role;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ResponsableEmployeMapperTest {

    @Test
    void toResponseMapsEmployeeDepartmentUserAndBalance() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        Departement departement = TestFixtures.departement(3L, "RH");
        employe.setDepartement(departement);
        SoldeConge solde = TestFixtures.solde(employe, 2026, 12D, 18D);

        ResponsableEmployeResponse response = new ResponsableEmployeMapper().toResponse(employe, solde, 2025);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUtilisateurId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("user10@example.test");
        assertThat(response.getRole()).isEqualTo("EMPLOYE");
        assertThat(response.getDepartementNom()).isEqualTo("RH");
        assertThat(response.getSoldeActuel()).isEqualTo(12D);
        assertThat(response.getAnneeSolde()).isEqualTo(2026);
    }

    @Test
    void toResponseUsesRequestedYearWhenSoldeIsNull() {
        ResponsableEmployeResponse response = new ResponsableEmployeMapper().toResponse(null, null, 2026);

        assertThat(response.getId()).isNull();
        assertThat(response.getAnneeSolde()).isEqualTo(2026);
        assertThat(response.getSoldeActuel()).isNull();
    }
}
