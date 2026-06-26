package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.model.SoldeConge;
import java.time.LocalDate;

public interface ISoldeCongeService {
    SoldeCongeResponse getSoldeCourant();

    SoldeConge getOrCreateSolde(Long employeId, Integer annee);

    Double calculerJoursOuvres(LocalDate dateDebut, LocalDate dateFin);

    Double calculerJoursCongeOuvres(LocalDate dateDebut, LocalDate dateFin);

    void deduireSolde(Long employeId, Integer annee, Double jours);

    void restaurerSolde(Long employeId, Integer annee, Double jours);
}
