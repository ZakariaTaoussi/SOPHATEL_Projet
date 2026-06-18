package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.DemandeCongeResponse;

public interface IDemandeCongeService extends ISelfDemandeCongeService {

    DemandeCongeResponse passerEnModificationEmploye(Long demandeId);
}
