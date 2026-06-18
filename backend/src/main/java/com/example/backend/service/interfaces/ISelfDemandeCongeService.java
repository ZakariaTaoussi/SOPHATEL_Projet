package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.dto.demande.SoldeCongeResponse;
import java.util.List;

public interface ISelfDemandeCongeService {
    DemandeCongeResponse creerBrouillon(DemandeCongeCreateRequest request);

    DemandeCongeResponse modifierDemande(Long demandeId, DemandeCongeUpdateRequest request);

    DemandeCongeResponse submitDemande(Long demandeId);

    DemandeCongeResponse passerEnModification(Long demandeId);

    DemandeCongeResponse annulerDemande(Long demandeId);

    List<DemandeCongeResponse> mesDemandes();

    DemandeCongeResponse getMaDemande(Long demandeId);

    SoldeCongeResponse getSoldeCourant();
}
