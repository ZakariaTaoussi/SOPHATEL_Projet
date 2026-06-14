package com.example.backend.repository;

import com.example.backend.model.JourFerie;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourFerieRepository extends JpaRepository<JourFerie, Long> {
    List<JourFerie> findByAgendaAnneeOrderByDateDebutAsc(Integer annee);

    void deleteByAgendaId(Long agendaId);
}
