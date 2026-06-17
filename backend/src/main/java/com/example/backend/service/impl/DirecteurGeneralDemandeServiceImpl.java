package com.example.backend.service.impl;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.interfaces.IDirecteurGeneralDemandeService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DirecteurGeneralDemandeServiceImpl implements IDirecteurGeneralDemandeService {

    private final DemandeCongeRepository demandeCongeRepository;
    private final DemandeCongeMapper demandeCongeMapper;

    public DirecteurGeneralDemandeServiceImpl(
            DemandeCongeRepository demandeCongeRepository,
            DemandeCongeMapper demandeCongeMapper) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.demandeCongeMapper = demandeCongeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponsableDemandeResponse> getDemandesAValider() {
        return demandeCongeRepository.findByStatusOrderByUpdatedAtDesc(StatusDemande.VALIDE_RESPONSABLE).stream()
                .map(demandeCongeMapper::toResponsableResponse)
                .toList();
    }
}
