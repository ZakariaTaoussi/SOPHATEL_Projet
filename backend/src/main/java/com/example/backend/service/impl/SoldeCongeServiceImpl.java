package com.example.backend.service.impl;

import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.SoldeCongeMapper;
import com.example.backend.model.Employe;
import com.example.backend.model.JourCalendrier;
import com.example.backend.model.SoldeConge;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.JourCalendrierRepository;
import com.example.backend.repository.SoldeCongeRepository;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoldeCongeServiceImpl implements ISoldeCongeService {

    private static final double SOLDE_ANNUEL_PAR_DEFAUT = 18D;
    private static final double JOURS_ACQUIS_PAR_MOIS = 1.5D;

    private final SoldeCongeRepository soldeCongeRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final JourCalendrierRepository jourCalendrierRepository;
    private final SoldeCongeMapper soldeCongeMapper;
    private final EmployeConnecteProvider employeConnecteProvider;

    public SoldeCongeServiceImpl(
            SoldeCongeRepository soldeCongeRepository,
            DemandeCongeRepository demandeCongeRepository,
            EmployeRepository employeRepository,
            JourCalendrierRepository jourCalendrierRepository,
            SoldeCongeMapper soldeCongeMapper,
            EmployeConnecteProvider employeConnecteProvider) {
        this.soldeCongeRepository = soldeCongeRepository;
        this.demandeCongeRepository = demandeCongeRepository;
        this.employeRepository = employeRepository;
        this.jourCalendrierRepository = jourCalendrierRepository;
        this.soldeCongeMapper = soldeCongeMapper;
        this.employeConnecteProvider = employeConnecteProvider;
    }

    @Override
    @Transactional
    public SoldeCongeResponse getSoldeCourant() {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        SoldeConge solde = getOrCreateSolde(employe.getIdEmp(), LocalDate.now().getYear());
        synchroniserSoldeDisponible(solde);
        return soldeCongeMapper.toResponse(solde);
    }

    @Override
    @Transactional
    public SoldeConge getOrCreateSolde(Long employeId, Integer annee) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseGet(() -> creerSoldeAnnuel(employeId, annee));
        return synchroniserSoldeDisponible(solde);
    }

    @Override
    public Double calculerJoursCongeOuvres(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            throw new InvalidBusinessRequestException("Les dates de debut et de fin sont obligatoires");
        }
        if (dateDebut.isAfter(dateFin)) {
            throw new InvalidBusinessRequestException("La date de debut ne peut pas etre apres la date de fin");
        }

        Set<LocalDate> joursFeries = jourCalendrierRepository.findByDateBetweenOrderByDateAsc(dateDebut, dateFin).stream()
                .filter(jour -> jour.getJourFerie() != null)
                .map(JourCalendrier::getDate)
                .collect(Collectors.toSet());

        double total = 0D;
        LocalDate current = dateDebut;
        while (!current.isAfter(dateFin)) {
            if (isJourOuvre(current) && !joursFeries.contains(current)) {
                total++;
            }
            current = current.plusDays(1);
        }
        return total;
    }

    @Override
    @Transactional
    public void deduireSolde(Long employeId, Integer annee, Double jours) {
        if (jours == null || jours <= 0D) {
            return;
        }

        SoldeConge solde = getOrCreateSolde(employeId, annee);
        if (solde.getSoldeActuel() < jours) {
            throw new InvalidBusinessRequestException("Solde insuffisant");
        }

        solde.setSoldeActuel(solde.getSoldeActuel() - jours);
        soldeCongeRepository.save(solde);
    }

    @Override
    @Transactional
    public void restaurerSolde(Long employeId, Integer annee, Double jours) {
        if (jours == null || jours <= 0D) {
            return;
        }

        SoldeConge solde = getOrCreateSolde(employeId, annee);
        double soldeRestaure = Math.min(solde.getSoldeActuel() + jours, calculerJoursAcquis(annee));
        solde.setSoldeActuel(soldeRestaure);
        soldeCongeRepository.save(solde);
    }

    private SoldeConge creerSoldeAnnuel(Long employeId, Integer annee) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable"));

        SoldeConge solde = new SoldeConge();
        solde.setEmploye(employe);
        solde.setAnnee(annee);
        solde.setSoldeTotal(SOLDE_ANNUEL_PAR_DEFAUT);
        solde.setSoldeActuel(calculerJoursAcquis(annee));
        return soldeCongeRepository.save(solde);
    }

    private SoldeConge synchroniserSoldeDisponible(SoldeConge solde) {
        Long employeId = solde.getEmploye().getIdEmp();
        Integer annee = solde.getAnnee();
        double joursAcquis = calculerJoursAcquis(annee);
        double joursDeduits = demandeCongeRepository.sumJoursDeduitsByEmployeAndDateDebutBetween(
                employeId,
                LocalDate.of(annee, 1, 1),
                LocalDate.of(annee, 12, 31));
        double soldeDisponible = Math.max(joursAcquis - joursDeduits, 0D);

        solde.setSoldeTotal(SOLDE_ANNUEL_PAR_DEFAUT);
        solde.setSoldeActuel(soldeDisponible);
        return soldeCongeRepository.save(solde);
    }

    private double calculerJoursAcquis(Integer annee) {
        LocalDate today = LocalDate.now();
        if (annee < today.getYear()) {
            return SOLDE_ANNUEL_PAR_DEFAUT;
        }
        if (annee > today.getYear()) {
            return 0D;
        }

        return Math.min(today.getMonthValue() * JOURS_ACQUIS_PAR_MOIS, SOLDE_ANNUEL_PAR_DEFAUT);
    }

    private boolean isJourOuvre(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
