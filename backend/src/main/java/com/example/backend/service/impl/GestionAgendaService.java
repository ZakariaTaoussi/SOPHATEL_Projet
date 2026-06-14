package com.example.backend.service.impl;

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
import com.example.backend.service.interfaces.IGestionAgenda;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionAgendaService implements IGestionAgenda {

    private final AgendaRepository agendaRepository;
    private final JourCalendrierRepository jourCalendrierRepository;
    private final JourFerieRepository jourFerieRepository;

    public GestionAgendaService(
            AgendaRepository agendaRepository,
            JourCalendrierRepository jourCalendrierRepository,
            JourFerieRepository jourFerieRepository) {
        this.agendaRepository = agendaRepository;
        this.jourCalendrierRepository = jourCalendrierRepository;
        this.jourFerieRepository = jourFerieRepository;
    }

    @Override
    @Transactional
    public AgendaResponse creerAgenda(Integer annee) {
        if (annee == null) {
            throw new InvalidBusinessRequestException("L'annee est obligatoire");
        }
        if (annee < Year.now().getValue()) {
            throw new InvalidBusinessRequestException("L'annee ne peut pas etre inferieure a l'annee actuelle");
        }
        if (agendaRepository.existsByAnnee(annee)) {
            throw new ResourceAlreadyExistsException("Un agenda existe deja pour cette annee");
        }

        Agenda agenda = new Agenda();
        agenda.setAnnee(annee);
        LocalDate date = LocalDate.of(annee, 1, 1);
        LocalDate end = LocalDate.of(annee, 12, 31);
        while (!date.isAfter(end)) {
            JourCalendrier jour = new JourCalendrier();
            jour.setDate(date);
            jour.setAgenda(agenda);
            agenda.getJoursCalendrier().add(jour);
            date = date.plusDays(1);
        }

        return toResponse(agendaRepository.save(agenda));
    }

    @Override
    public List<AgendaResponse> consulterAgendas() {
        return agendaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AgendaResponse consulterAgendaParAnnee(Integer annee) {
        return toResponse(findByAnnee(annee));
    }

    @Override
    public List<JourCalendrierResponse> consulterJoursCalendrier(Integer annee) {
        findByAnnee(annee);
        return jourCalendrierRepository.findByAgendaAnneeOrderByDateAsc(annee).stream()
                .map(this::toJourResponse)
                .toList();
    }

    @Override
    @Transactional
    public void supprimerAgenda(Long id) {
        Agenda agenda = agendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agenda introuvable"));
        jourCalendrierRepository.findByAgendaId(id).forEach(jour -> jour.setJourFerie(null));
        jourFerieRepository.deleteByAgendaId(id);
        agendaRepository.delete(agenda);
    }

    private Agenda findByAnnee(Integer annee) {
        return agendaRepository.findByAnnee(annee)
                .orElseThrow(() -> new ResourceNotFoundException("Agenda introuvable pour cette annee"));
    }

    private AgendaResponse toResponse(Agenda agenda) {
        return new AgendaResponse(agenda.getId(), agenda.getAnnee());
    }

    private JourCalendrierResponse toJourResponse(JourCalendrier jour) {
        JourFerie jourFerie = jour.getJourFerie();
        return new JourCalendrierResponse(
                jour.getId(),
                jour.getDate(),
                jourFerie == null ? null : jourFerie.getId(),
                jourFerie == null ? null : jourFerie.getNom(),
                jourFerie == null ? null : jourFerie.getDescription());
    }
}
