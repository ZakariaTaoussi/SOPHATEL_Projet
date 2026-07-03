package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import java.util.List;
import org.junit.jupiter.api.Test;

class DepartementTest {

    @Test
    void shouldStoreDepartementRelations() {
        Employe responsable = new Employe();
        responsable.setIdEmp(10L);
        Employe employe = new Employe();
        employe.setIdEmp(11L);

        Departement departement = new Departement();
        departement.setId(2L);
        departement.setNom("RH");
        departement.setResponsable(responsable);
        departement.setEmployes(List.of(employe));

        assertThat(departement.getId()).isEqualTo(2L);
        assertThat(departement.getNom()).isEqualTo("RH");
        assertThat(departement.getResponsable()).isSameAs(responsable);
        assertThat(departement.getEmployes()).containsExactly(employe);
    }
}
