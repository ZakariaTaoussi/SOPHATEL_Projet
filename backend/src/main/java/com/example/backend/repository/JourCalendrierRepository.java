package com.example.backend.repository;

import com.example.backend.model.JourCalendrier;
import com.example.backend.model.JourFerie;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourCalendrierRepository extends JpaRepository<JourCalendrier, Long> {
    List<JourCalendrier> findByAgendaId(Long agendaId);

    List<JourCalendrier> findByAgendaAnneeOrderByDateAsc(Integer annee);

    List<JourCalendrier> findByDateBetweenOrderByDateAsc(LocalDate dateDebut, LocalDate dateFin);

    List<JourCalendrier> findByJourFerie(JourFerie jourFerie);
}
