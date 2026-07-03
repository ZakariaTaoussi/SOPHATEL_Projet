package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.admin.CreateJourFerieRequest;
import com.example.backend.dto.admin.JourFerieResponse;
import com.example.backend.dto.admin.UpdateJourFerieRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceConflictException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Agenda;
import com.example.backend.model.JourCalendrier;
import com.example.backend.model.JourFerie;
import com.example.backend.repository.AgendaRepository;
import com.example.backend.repository.JourCalendrierRepository;
import com.example.backend.repository.JourFerieRepository;
import com.example.backend.service.impl.GestionJourFerieService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestionJourFerieServiceTest {

    @Mock
    private JourFerieRepository jourFerieRepository;

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private JourCalendrierRepository jourCalendrierRepository;

    @Test
    void creerJourFeriePersistsHolidayAndAssignsCalendarDays() {
        Agenda agenda = agenda(2026);
        JourCalendrier first = jour(LocalDate.of(2026, 1, 1), null);
        JourCalendrier second = jour(LocalDate.of(2026, 1, 2), null);
        CreateJourFerieRequest request = request("  Fete  ", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
        when(agendaRepository.findByAnnee(2026)).thenReturn(Optional.of(agenda));
        when(jourCalendrierRepository.findByDateBetweenOrderByDateAsc(request.getDateDebut(), request.getDateFin()))
                .thenReturn(List.of(first, second));
        when(jourFerieRepository.save(any(JourFerie.class))).thenAnswer(invocation -> {
            JourFerie jourFerie = invocation.getArgument(0);
            jourFerie.setId(5L);
            return jourFerie;
        });

        JourFerieResponse response = service().creerJourFerie(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.nom()).isEqualTo("Fete");
        assertThat(first.getJourFerie().getId()).isEqualTo(5L);
        assertThat(second.getJourFerie().getId()).isEqualTo(5L);
        verify(jourCalendrierRepository).saveAll(List.of(first, second));
    }

    @Test
    void creerJourFerieRejectsInvalidDatesAndMissingAgenda() {
        CreateJourFerieRequest invalidDates = request("Fete", LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 1));
        assertThatThrownBy(() -> service().creerJourFerie(invalidDates))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("La date debut ne peut pas etre apres la date fin");

        CreateJourFerieRequest missingAgenda = request("Fete", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1));
        when(agendaRepository.findByAnnee(2026)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().creerJourFerie(missingAgenda))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Agenda introuvable pour cette annee");
    }

    @Test
    void creerJourFerieRejectsOverlappingHoliday() {
        Agenda agenda = agenda(2026);
        JourFerie existing = jourFerie(9L, agenda);
        CreateJourFerieRequest request = request("Fete", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1));
        when(agendaRepository.findByAnnee(2026)).thenReturn(Optional.of(agenda));
        when(jourCalendrierRepository.findByDateBetweenOrderByDateAsc(request.getDateDebut(), request.getDateFin()))
                .thenReturn(List.of(jour(LocalDate.of(2026, 1, 1), existing)));

        assertThatThrownBy(() -> service().creerJourFerie(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Cette periode chevauche un autre jour ferie");

        verify(jourFerieRepository, never()).save(any());
    }

    @Test
    void modifierJourFerieClearsOldDaysAndAssignsNewDays() {
        Agenda agenda = agenda(2026);
        JourFerie existing = jourFerie(9L, agenda);
        JourCalendrier oldDay = jour(LocalDate.of(2026, 1, 1), existing);
        JourCalendrier newDay = jour(LocalDate.of(2026, 2, 1), null);
        UpdateJourFerieRequest request = new UpdateJourFerieRequest();
        request.setNom("Nouvelle fete");
        request.setDateDebut(LocalDate.of(2026, 2, 1));
        request.setDateFin(LocalDate.of(2026, 2, 1));
        request.setDescription("desc");
        when(jourFerieRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(agendaRepository.findByAnnee(2026)).thenReturn(Optional.of(agenda));
        when(jourCalendrierRepository.findByJourFerie(existing)).thenReturn(List.of(oldDay));
        when(jourCalendrierRepository.findByDateBetweenOrderByDateAsc(request.getDateDebut(), request.getDateFin()))
                .thenReturn(List.of(newDay));
        when(jourFerieRepository.save(existing)).thenReturn(existing);

        JourFerieResponse response = service().modifierJourFerie(9L, request);

        assertThat(oldDay.getJourFerie()).isNull();
        assertThat(newDay.getJourFerie()).isSameAs(existing);
        assertThat(response.nom()).isEqualTo("Nouvelle fete");
        verify(jourCalendrierRepository).saveAll(List.of(oldDay));
        verify(jourCalendrierRepository).saveAll(List.of(newDay));
    }

    @Test
    void supprimerJourFerieClearsCalendarLinksThenDeletes() {
        Agenda agenda = agenda(2026);
        JourFerie jourFerie = jourFerie(9L, agenda);
        JourCalendrier linkedDay = jour(LocalDate.of(2026, 1, 1), jourFerie);
        when(jourFerieRepository.findById(9L)).thenReturn(Optional.of(jourFerie));
        when(jourCalendrierRepository.findByJourFerie(jourFerie)).thenReturn(List.of(linkedDay));

        service().supprimerJourFerie(9L);

        assertThat(linkedDay.getJourFerie()).isNull();
        verify(jourCalendrierRepository).saveAll(List.of(linkedDay));
        verify(jourFerieRepository).delete(jourFerie);
    }

    private GestionJourFerieService service() {
        return new GestionJourFerieService(jourFerieRepository, agendaRepository, jourCalendrierRepository);
    }

    private CreateJourFerieRequest request(String nom, LocalDate dateDebut, LocalDate dateFin) {
        CreateJourFerieRequest request = new CreateJourFerieRequest();
        request.setNom(nom);
        request.setDateDebut(dateDebut);
        request.setDateFin(dateFin);
        request.setDescription("Description");
        return request;
    }

    private Agenda agenda(Integer annee) {
        Agenda agenda = new Agenda();
        agenda.setId(1L);
        agenda.setAnnee(annee);
        return agenda;
    }

    private JourCalendrier jour(LocalDate date, JourFerie jourFerie) {
        JourCalendrier jour = new JourCalendrier();
        jour.setDate(date);
        jour.setJourFerie(jourFerie);
        return jour;
    }

    private JourFerie jourFerie(Long id, Agenda agenda) {
        JourFerie jourFerie = new JourFerie();
        jourFerie.setId(id);
        jourFerie.setNom("Fete");
        jourFerie.setDateDebut(LocalDate.of(agenda.getAnnee(), 1, 1));
        jourFerie.setDateFin(LocalDate.of(agenda.getAnnee(), 1, 1));
        jourFerie.setAgenda(agenda);
        return jourFerie;
    }
}
