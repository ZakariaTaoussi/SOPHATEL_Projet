package com.example.backend.mapper;

import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import org.springframework.stereotype.Component;

@Component
public class SoldeCongeMapper {

    public SoldeCongeResponse toResponse(SoldeConge soldeConge) {
        Employe employe = soldeConge.getEmploye();
        return new SoldeCongeResponse(
                soldeConge.getId(),
                employe == null ? null : employe.getIdEmp(),
                soldeConge.getAnnee(),
                soldeConge.getSoldeActuel(),
                soldeConge.getSoldeTotal());
    }
}
