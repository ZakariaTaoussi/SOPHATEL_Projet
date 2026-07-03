package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Agenda;
import com.example.backend.model.JourCalendrier;
import com.example.backend.model.JourFerie;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class JourFerieTest {

    @Test
    void shouldStoreJourFerieFields() {
        Agenda agenda = new Agenda();
        JourCalendrier jourCalendrier = new JourCalendrier();

        JourFerie jourFerie = new JourFerie();
        jourFerie.setId(5L);
        jourFerie.setNom("Fete");
        jourFerie.setDateDebut(LocalDate.of(2026, 1, 1));
        jourFerie.setDateFin(LocalDate.of(2026, 1, 2));
        jourFerie.setDescription("Description");
        jourFerie.setAgenda(agenda);
        jourFerie.setJoursCalendrier(List.of(jourCalendrier));

        assertThat(jourFerie.getId()).isEqualTo(5L);
        assertThat(jourFerie.getNom()).isEqualTo("Fete");
        assertThat(jourFerie.getDateDebut()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(jourFerie.getDateFin()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(jourFerie.getDescription()).isEqualTo("Description");
        assertThat(jourFerie.getAgenda()).isSameAs(agenda);
        assertThat(jourFerie.getJoursCalendrier()).containsExactly(jourCalendrier);
    }
}
