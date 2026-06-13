package com.example.backend.service.interfaces;

import com.example.backend.dto.admin.CreateJourFerieRequest;
import com.example.backend.dto.admin.JourFerieResponse;
import com.example.backend.dto.admin.UpdateJourFerieRequest;
import java.util.List;

public interface IGestionJourFerie {
    JourFerieResponse creerJourFerie(CreateJourFerieRequest request);

    JourFerieResponse modifierJourFerie(Long id, UpdateJourFerieRequest request);

    void supprimerJourFerie(Long id);

    List<JourFerieResponse> consulterJoursFeriesParAnnee(Integer annee);

    JourFerieResponse consulterJourFerie(Long id);
}
