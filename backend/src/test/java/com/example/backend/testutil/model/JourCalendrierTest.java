package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Agenda;
import com.example.backend.model.JourCalendrier;
import com.example.backend.model.JourFerie;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class JourCalendrierTest {

    @Test
    void shouldStoreJourCalendrierFields() {
        Agenda agenda = new Agenda();
        JourFerie jourFerie = new JourFerie();

        JourCalendrier jour = new JourCalendrier();
        jour.setId(3L);
        jour.setDate(LocalDate.of(2026, 1, 1));
        jour.setAgenda(agenda);
        jour.setJourFerie(jourFerie);

        assertThat(jour.getId()).isEqualTo(3L);
        assertThat(jour.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(jour.getAgenda()).isSameAs(agenda);
        assertThat(jour.getJourFerie()).isSameAs(jourFerie);
    }
}
