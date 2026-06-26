package com.example.backend.controller.rh;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.dto.rh.RhDemandeSuiviResponse;
import com.example.backend.dto.rh.RhDepartementResponse;
import com.example.backend.service.interfaces.IRhSuiviDemandesService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh/suivi")
public class RhSuiviDemandesController {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final IRhSuiviDemandesService rhSuiviDemandesService;

    public RhSuiviDemandesController(IRhSuiviDemandesService rhSuiviDemandesService) {
        this.rhSuiviDemandesService = rhSuiviDemandesService;
    }

    @GetMapping("/conges")
    public ResponseEntity<PageResponse<RhDemandeSuiviResponse>> getCongesValidesDg(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departementId) {
        return ResponseEntity.ok(rhSuiviDemandesService.getCongesValidesDg(page, size, annee, mois, search, departementId));
    }

    @GetMapping("/absences")
    public ResponseEntity<PageResponse<RhDemandeSuiviResponse>> getAbsencesValideesDg(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departementId) {
        return ResponseEntity.ok(rhSuiviDemandesService.getAbsencesValideesDg(page, size, annee, mois, search, departementId));
    }

    @GetMapping("/conges/export")
    public ResponseEntity<byte[]> exportCongesValidesDg(
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departementId) {
        return excelResponse(
                rhSuiviDemandesService.exportCongesValidesDgExcel(annee, mois, search, departementId),
                "conges-valides-dg" + suffix(annee, mois) + ".xlsx");
    }

    @GetMapping("/absences/export")
    public ResponseEntity<byte[]> exportAbsencesValideesDg(
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departementId) {
        return excelResponse(
                rhSuiviDemandesService.exportAbsencesValideesDgExcel(annee, mois, search, departementId),
                "absences-valides-dg" + suffix(annee, mois) + ".xlsx");
    }

    @GetMapping("/conges/{id}/impression")
    public ResponseEntity<DemandeCongeImpressionResponse> getCongePourImpression(@PathVariable Long id) {
        return ResponseEntity.ok(rhSuiviDemandesService.imprimerCongeValideDg(id));
    }

    @GetMapping("/absences/{id}/impression")
    public ResponseEntity<DemandeCongeImpressionResponse> getAbsencePourImpression(@PathVariable Long id) {
        return ResponseEntity.ok(rhSuiviDemandesService.imprimerAbsenceValideeDg(id));
    }

    @GetMapping("/departements")
    public ResponseEntity<List<RhDepartementResponse>> getDepartements() {
        return ResponseEntity.ok(rhSuiviDemandesService.getDepartements());
    }

    private ResponseEntity<byte[]> excelResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .contentType(EXCEL_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }

    private String suffix(Integer annee, Integer mois) {
        if (annee == null) {
            return "";
        }
        if (mois == null) {
            return "-" + annee;
        }
        return "-" + annee + "-" + String.format("%02d", mois);
    }
}
