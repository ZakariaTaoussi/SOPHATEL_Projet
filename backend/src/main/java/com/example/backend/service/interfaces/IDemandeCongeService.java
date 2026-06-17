package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import java.util.List;

public interface IDemandeCongeService {
    DemandeCongeResponse creerBrouillon(DemandeCongeCreateRequest request);

    DemandeCongeResponse modifierDemande(Long demandeId, DemandeCongeUpdateRequest request);

    DemandeCongeResponse submitDemande(Long demandeId);

    DemandeCongeResponse passerEnModificationEmploye(Long demandeId);

    DemandeCongeResponse annulerDemande(Long demandeId);

    List<DemandeCongeResponse> mesDemandes();

    DemandeCongeResponse getMaDemande(Long demandeId);

    List<DemandeCongeResponse> demandesAValiderPourResponsable();
}
