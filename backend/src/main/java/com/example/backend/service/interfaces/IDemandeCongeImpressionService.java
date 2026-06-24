package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.DemandeCongeImpressionResponse;

public interface IDemandeCongeImpressionService {
    DemandeCongeImpressionResponse getDemandePourImpression(Long demandeId);
}
