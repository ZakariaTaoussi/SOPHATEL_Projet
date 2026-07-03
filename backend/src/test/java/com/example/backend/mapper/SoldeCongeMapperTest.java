package com.example.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.enums.Role;
import com.example.backend.testutil.TestFixtures;
import org.junit.jupiter.api.Test;

class SoldeCongeMapperTest {

    @Test
    void toResponseMapsSoldeAndEmployeeId() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        SoldeConge solde = TestFixtures.solde(employe, 2026, 12D, 18D);

        SoldeCongeResponse response = new SoldeCongeMapper().toResponse(solde);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmpId()).isEqualTo(10L);
        assertThat(response.getAnnee()).isEqualTo(2026);
        assertThat(response.getSoldeActuel()).isEqualTo(12D);
        assertThat(response.getSoldeTotal()).isEqualTo(18D);
    }
}
