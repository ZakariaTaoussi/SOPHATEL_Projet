package com.example.backend.service.interfaces;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.dto.rh.RhDemandeSuiviResponse;
import com.example.backend.dto.rh.RhDepartementResponse;
import java.util.List;

public interface IRhSuiviDemandesService {
    PageResponse<RhDemandeSuiviResponse> getCongesValidesDg(
            int page,
            int size,
            Integer annee,
            Integer mois,
            String search,
            Long departementId);

    PageResponse<RhDemandeSuiviResponse> getAbsencesValideesDg(
            int page,
            int size,
            Integer annee,
            Integer mois,
            String search,
            Long departementId);

    byte[] exportCongesValidesDgExcel(Integer annee, Integer mois, String search, Long departementId);

    byte[] exportAbsencesValideesDgExcel(Integer annee, Integer mois, String search, Long departementId);

    DemandeCongeImpressionResponse imprimerCongeValideDg(Long demandeId);

    DemandeCongeImpressionResponse imprimerAbsenceValideeDg(Long demandeId);

    List<RhDepartementResponse> getDepartements();
}
