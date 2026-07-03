package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.enums.StatusDemande;
import org.junit.jupiter.api.Test;

class StatusDemandeTest {

    @Test
    void shouldExposeExpectedValues() {
        assertThat(StatusDemande.values())
                .containsExactly(
                        StatusDemande.BROUILLON,
                        StatusDemande.VALIDE_EMPLOYE,
                        StatusDemande.VALIDE_RESPONSABLE,
                        StatusDemande.VALIDE_DG,
                        StatusDemande.MODIFICATION_EMPLOYE,
                        StatusDemande.MODIFICATION_RESPONSABLE,
                        StatusDemande.MODIFICATION_DG,
                        StatusDemande.ANNULE,
                        StatusDemande.REFUSE_RESPONSABLE,
                        StatusDemande.REFUSE_DG);
    }
}
