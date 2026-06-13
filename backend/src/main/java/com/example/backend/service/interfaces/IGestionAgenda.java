package com.example.backend.service.interfaces;

import com.example.backend.dto.admin.AgendaResponse;
import com.example.backend.dto.admin.JourCalendrierResponse;
import java.util.List;

public interface IGestionAgenda {
    AgendaResponse creerAgenda(Integer annee);

    List<AgendaResponse> consulterAgendas();

    AgendaResponse consulterAgendaParAnnee(Integer annee);

    List<JourCalendrierResponse> consulterJoursCalendrier(Integer annee);

    void supprimerAgenda(Long id);
}
