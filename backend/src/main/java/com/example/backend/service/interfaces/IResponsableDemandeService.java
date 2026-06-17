package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.dto.demande.ResponsableValidationDemandeRequest;
import java.util.List;

public interface IResponsableDemandeService {
    List<ResponsableDemandeResponse> getDemandesAValider();

    ResponsableDemandeResponse getDemandeAValiderById(Long demandeId);

    ResponsableDemandeResponse validerDemandeParResponsable(
            Long demandeId,
            ResponsableValidationDemandeRequest request);

    ResponsableDemandeResponse refuserDemandeParResponsable(Long demandeId);

    ResponsableDemandeResponse passerEnModificationResponsable(Long demandeId);
}
