package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Agenda;
import com.example.backend.model.JourCalendrier;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgendaTest {

    @Test
    void shouldStoreAgendaFields() {
        JourCalendrier jour = new JourCalendrier();
        jour.setDate(LocalDate.of(2026, 1, 1));

        Agenda agenda = new Agenda();
        agenda.setId(1L);
        agenda.setAnnee(2026);
        agenda.setJoursCalendrier(List.of(jour));

        assertThat(agenda.getId()).isEqualTo(1L);
        assertThat(agenda.getAnnee()).isEqualTo(2026);
        assertThat(agenda.getJoursCalendrier()).containsExactly(jour);
    }
}
