package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.mapper.SoldeCongeMapper;
import com.example.backend.model.Employe;
import com.example.backend.model.JourCalendrier;
import com.example.backend.model.JourFerie;
import com.example.backend.model.SoldeConge;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.JourCalendrierRepository;
import com.example.backend.repository.SoldeCongeRepository;
import com.example.backend.service.impl.EmployeConnecteProvider;
import com.example.backend.service.impl.SoldeCongeServiceImpl;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SoldeCongeServiceImplTest {

    @Mock
    private SoldeCongeRepository soldeCongeRepository;

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private JourCalendrierRepository jourCalendrierRepository;

    @Mock
    private EmployeConnecteProvider employeConnecteProvider;

    private SoldeCongeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SoldeCongeServiceImpl(
                soldeCongeRepository,
                demandeCongeRepository,
                employeRepository,
                jourCalendrierRepository,
                new SoldeCongeMapper(),
                employeConnecteProvider);
    }

    @Test
    void calculerJoursOuvresExcludesWeekendAndHolidays() {
        JourCalendrier holiday = new JourCalendrier();
        holiday.setDate(LocalDate.of(2026, 7, 7));
        holiday.setJourFerie(new JourFerie());
        when(jourCalendrierRepository.findByDateBetweenOrderByDateAsc(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 12))).thenReturn(List.of(holiday));

        Double jours = service.calculerJoursOuvres(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12));

        assertThat(jours).isEqualTo(4D);
    }

    @Test
    void calculerJoursOuvresRejectsInvalidDates() {
        assertThatThrownBy(() -> service.calculerJoursOuvres(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 6)))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Dates invalides");
    }

    @Test
    void deduireSoldeRejectsInsufficientBalance() {
        Employe employe = TestFixtures.employe(10L, com.example.backend.model.enums.Role.EMPLOYE);
        SoldeConge solde = new SoldeConge();
        solde.setEmploye(employe);
        solde.setAnnee(2026);
        solde.setSoldeActuel(2D);
        solde.setSoldeTotal(18D);

        when(soldeCongeRepository.findByEmployeIdAndAnnee(10L, 2026)).thenReturn(Optional.of(solde));
        when(demandeCongeRepository.sumJoursDeduitsByEmployeAndDateDebutBetween(
                10L,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31))).thenReturn(0D);
        when(soldeCongeRepository.save(solde)).thenReturn(solde);

        assertThatThrownBy(() -> service.deduireSolde(10L, 2026, 99D))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Solde insuffisant");
    }

    @Test
    void deduireSoldePersistsRemainingBalance() {
        Employe employe = TestFixtures.employe(10L, com.example.backend.model.enums.Role.EMPLOYE);
        SoldeConge solde = new SoldeConge();
        solde.setEmploye(employe);
        solde.setAnnee(LocalDate.now().getYear());
        solde.setSoldeActuel(18D);
        solde.setSoldeTotal(18D);

        when(soldeCongeRepository.findByEmployeIdAndAnnee(10L, LocalDate.now().getYear())).thenReturn(Optional.of(solde));
        when(demandeCongeRepository.sumJoursDeduitsByEmployeAndDateDebutBetween(
                10L,
                LocalDate.of(LocalDate.now().getYear(), 1, 1),
                LocalDate.of(LocalDate.now().getYear(), 12, 31))).thenReturn(0D);
        when(soldeCongeRepository.save(solde)).thenReturn(solde);

        service.deduireSolde(10L, LocalDate.now().getYear(), 3D);

        assertThat(solde.getSoldeActuel()).isEqualTo(Math.max(LocalDate.now().getMonthValue() * 1.5D - 3D, 0D));
        verify(soldeCongeRepository, org.mockito.Mockito.times(2)).save(solde);
    }
}
