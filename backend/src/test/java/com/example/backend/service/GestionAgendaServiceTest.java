package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.admin.AgendaResponse;
import com.example.backend.dto.admin.JourCalendrierResponse;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceAlreadyExistsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Agenda;
import com.example.backend.model.JourCalendrier;
import com.example.backend.model.JourFerie;
import com.example.backend.repository.AgendaRepository;
import com.example.backend.repository.JourCalendrierRepository;
import com.example.backend.repository.JourFerieRepository;
import com.example.backend.service.impl.GestionAgendaService;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestionAgendaServiceTest {

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private JourCalendrierRepository jourCalendrierRepository;

    @Mock
    private JourFerieRepository jourFerieRepository;

    @Test
    void creerAgendaGeneratesEveryDayOfYear() {
        int nextYear = Year.now().getValue() + 1;
        when(agendaRepository.existsByAnnee(nextYear)).thenReturn(false);
        when(agendaRepository.save(org.mockito.ArgumentMatchers.any(Agenda.class))).thenAnswer(invocation -> {
            Agenda agenda = invocation.getArgument(0);
            agenda.setId(1L);
            return agenda;
        });

        AgendaResponse response = service().creerAgenda(nextYear);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.annee()).isEqualTo(nextYear);
        org.mockito.ArgumentCaptor<Agenda> captor = org.mockito.ArgumentCaptor.forClass(Agenda.class);
        verify(agendaRepository).save(captor.capture());
        assertThat(captor.getValue().getJoursCalendrier()).hasSize(LocalDate.of(nextYear, 1, 1).isLeapYear() ? 366 : 365);
        assertThat(captor.getValue().getJoursCalendrier().get(0).getDate()).isEqualTo(LocalDate.of(nextYear, 1, 1));
    }

    @Test
    void creerAgendaRejectsPastYearAndDuplicateYear() {
        assertThatThrownBy(() -> service().creerAgenda(Year.now().getValue() - 1))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("L'annee ne peut pas etre inferieure a l'annee actuelle");

        int currentYear = Year.now().getValue();
        when(agendaRepository.existsByAnnee(currentYear)).thenReturn(true);
        assertThatThrownBy(() -> service().creerAgenda(currentYear))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Un agenda existe deja pour cette annee");
    }

    @Test
    void consulterJoursCalendrierMapsHolidayInformation() {
        Agenda agenda = agenda(2026);
        JourFerie jourFerie = jourFerie(9L, agenda);
        JourCalendrier jour = new JourCalendrier();
        jour.setId(1L);
        jour.setDate(LocalDate.of(2026, 1, 1));
        jour.setJourFerie(jourFerie);
        when(agendaRepository.findByAnnee(2026)).thenReturn(Optional.of(agenda));
        when(jourCalendrierRepository.findByAgendaAnneeOrderByDateAsc(2026)).thenReturn(List.of(jour));

        List<JourCalendrierResponse> response = service().consulterJoursCalendrier(2026);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).jourFerieId()).isEqualTo(9L);
        assertThat(response.get(0).jourFerieNom()).isEqualTo("Nouvel an");
    }

    @Test
    void supprimerAgendaDeletesHolidayLinksAndAgenda() {
        Agenda agenda = agenda(2026);
        JourCalendrier jour = new JourCalendrier();
        jour.setJourFerie(jourFerie(9L, agenda));
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(jourCalendrierRepository.findByAgendaId(1L)).thenReturn(List.of(jour));

        service().supprimerAgenda(1L);

        assertThat(jour.getJourFerie()).isNull();
        verify(jourFerieRepository).deleteByAgendaId(1L);
        verify(agendaRepository).delete(agenda);
    }

    @Test
    void consulterAgendaParAnneeRejectsMissingAgenda() {
        when(agendaRepository.findByAnnee(2026)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().consulterAgendaParAnnee(2026))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agenda introuvable pour cette annee");
    }

    private GestionAgendaService service() {
        return new GestionAgendaService(agendaRepository, jourCalendrierRepository, jourFerieRepository);
    }

    private Agenda agenda(Integer annee) {
        Agenda agenda = new Agenda();
        agenda.setId(1L);
        agenda.setAnnee(annee);
        return agenda;
    }

    private JourFerie jourFerie(Long id, Agenda agenda) {
        JourFerie jourFerie = new JourFerie();
        jourFerie.setId(id);
        jourFerie.setNom("Nouvel an");
        jourFerie.setDescription("Jour ferie");
        jourFerie.setDateDebut(LocalDate.of(agenda.getAnnee(), 1, 1));
        jourFerie.setDateFin(LocalDate.of(agenda.getAnnee(), 1, 1));
        jourFerie.setAgenda(agenda);
        return jourFerie;
    }
}
