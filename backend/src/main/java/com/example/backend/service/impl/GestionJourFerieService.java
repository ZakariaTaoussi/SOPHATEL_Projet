package com.example.backend.service.impl;

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
import com.example.backend.service.interfaces.IGestionJourFerie;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionJourFerieService implements IGestionJourFerie {

    private final JourFerieRepository jourFerieRepository;
    private final AgendaRepository agendaRepository;
    private final JourCalendrierRepository jourCalendrierRepository;

    public GestionJourFerieService(
            JourFerieRepository jourFerieRepository,
            AgendaRepository agendaRepository,
            JourCalendrierRepository jourCalendrierRepository) {
        this.jourFerieRepository = jourFerieRepository;
        this.agendaRepository = agendaRepository;
        this.jourCalendrierRepository = jourCalendrierRepository;
    }

    @Override
    @Transactional
    public JourFerieResponse creerJourFerie(CreateJourFerieRequest request) {
        validateRequest(request);
        Agenda agenda = findAgenda(request.getDateDebut(), request.getDateFin());
        List<JourCalendrier> jours = findJoursDisponibles(request.getDateDebut(), request.getDateFin(), null);

        JourFerie jourFerie = new JourFerie();
        applyValues(jourFerie, request, agenda);
        JourFerie saved = jourFerieRepository.save(jourFerie);
        jours.forEach(jour -> jour.setJourFerie(saved));
        jourCalendrierRepository.saveAll(jours);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public JourFerieResponse modifierJourFerie(Long id, UpdateJourFerieRequest request) {
        validateRequest(request);
        JourFerie jourFerie = findJourFerie(id);
        Agenda agenda = findAgenda(request.getDateDebut(), request.getDateFin());

        List<JourCalendrier> anciensJours = jourCalendrierRepository.findByJourFerie(jourFerie);
        anciensJours.forEach(jour -> jour.setJourFerie(null));
        jourCalendrierRepository.saveAll(anciensJours);

        List<JourCalendrier> nouveauxJours = findJoursDisponibles(request.getDateDebut(), request.getDateFin(), id);
        applyValues(jourFerie, request, agenda);
        JourFerie saved = jourFerieRepository.save(jourFerie);
        nouveauxJours.forEach(jour -> jour.setJourFerie(saved));
        jourCalendrierRepository.saveAll(nouveauxJours);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void supprimerJourFerie(Long id) {
        JourFerie jourFerie = findJourFerie(id);
        List<JourCalendrier> jours = jourCalendrierRepository.findByJourFerie(jourFerie);
        jours.forEach(jour -> jour.setJourFerie(null));
        jourCalendrierRepository.saveAll(jours);
        jourFerieRepository.delete(jourFerie);
    }

    @Override
    public List<JourFerieResponse> consulterJoursFeriesParAnnee(Integer annee) {
        if (!agendaRepository.existsByAnnee(annee)) {
            throw new ResourceNotFoundException("Agenda introuvable pour cette annee");
        }
        return jourFerieRepository.findByAgendaAnneeOrderByDateDebutAsc(annee).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public JourFerieResponse consulterJourFerie(Long id) {
        return toResponse(findJourFerie(id));
    }

    private void validateRequest(CreateJourFerieRequest request) {
        if (request.getNom() == null || request.getNom().isBlank()) {
            throw new InvalidBusinessRequestException("Le nom du jour ferie est obligatoire");
        }
        if (request.getDateDebut() == null || request.getDateFin() == null) {
            throw new InvalidBusinessRequestException("Les dates debut et fin sont obligatoires");
        }
        if (request.getDateDebut().isAfter(request.getDateFin())) {
            throw new InvalidBusinessRequestException("La date debut ne peut pas etre apres la date fin");
        }
    }

    private Agenda findAgenda(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut.getYear() != dateFin.getYear()) {
            throw new InvalidBusinessRequestException("Les dates doivent appartenir a la meme annee");
        }
        return agendaRepository.findByAnnee(dateDebut.getYear())
                .orElseThrow(() -> new ResourceNotFoundException("Agenda introuvable pour cette annee"));
    }

    private List<JourCalendrier> findJoursDisponibles(LocalDate dateDebut, LocalDate dateFin, Long currentJourFerieId) {
        List<JourCalendrier> jours = jourCalendrierRepository.findByDateBetweenOrderByDateAsc(dateDebut, dateFin);
        long expectedDays = dateFin.toEpochDay() - dateDebut.toEpochDay() + 1;
        if (jours.size() != expectedDays) {
            throw new InvalidBusinessRequestException("Toutes les dates doivent appartenir a un agenda existant");
        }

        boolean hasOverlap = jours.stream().anyMatch(jour ->
                jour.getJourFerie() != null && !jour.getJourFerie().getId().equals(currentJourFerieId));
        if (hasOverlap) {
            throw new ResourceConflictException("Cette periode chevauche un autre jour ferie");
        }
        return jours;
    }

    private JourFerie findJourFerie(Long id) {
        return jourFerieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jour ferie introuvable"));
    }

    private void applyValues(JourFerie jourFerie, CreateJourFerieRequest request, Agenda agenda) {
        jourFerie.setNom(request.getNom().trim());
        jourFerie.setDateDebut(request.getDateDebut());
        jourFerie.setDateFin(request.getDateFin());
        jourFerie.setDescription(request.getDescription());
        jourFerie.setAgenda(agenda);
    }

    private JourFerieResponse toResponse(JourFerie jourFerie) {
        return new JourFerieResponse(
                jourFerie.getId(),
                jourFerie.getNom(),
                jourFerie.getDateDebut(),
                jourFerie.getDateFin(),
                jourFerie.getDescription());
    }
}
