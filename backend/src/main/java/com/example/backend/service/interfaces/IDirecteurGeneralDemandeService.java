package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.DirecteurGeneralDemandeResponse;
import com.example.backend.dto.demande.DirecteurGeneralValidationDemandeRequest;
import java.util.List;

public interface IDirecteurGeneralDemandeService {

    List<DirecteurGeneralDemandeResponse> getDemandesAValider();

    List<DirecteurGeneralDemandeResponse> getAbsencesAValider();

    List<DirecteurGeneralDemandeResponse> getDemandesValidees();

    DirecteurGeneralDemandeResponse getDemandeAValiderById(Long demandeId);

    DirecteurGeneralDemandeResponse validerDemandeParDg(
            Long demandeId,
            DirecteurGeneralValidationDemandeRequest request);

    DirecteurGeneralDemandeResponse refuserDemandeParDg(Long demandeId);

    DirecteurGeneralDemandeResponse passerEnModificationDg(Long demandeId);
}
