package com.example.backend.controller.employe;

import com.example.backend.dto.demande.AbsenceStatsResponse;
import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.service.interfaces.ISelfDemandeCongeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employe")
public class EmployeDemandeCongeController {

    private final ISelfDemandeCongeService demandeCongeService;
    private final ISoldeCongeService soldeCongeService;

    public EmployeDemandeCongeController(
            ISelfDemandeCongeService demandeCongeService,
            ISoldeCongeService soldeCongeService) {
        this.demandeCongeService = demandeCongeService;
        this.soldeCongeService = soldeCongeService;
    }

    @GetMapping("/demandes")
    public ResponseEntity<List<DemandeCongeResponse>> mesDemandes() {
        return ResponseEntity.ok(demandeCongeService.mesDemandes());
    }

    @GetMapping("/absences")
    public ResponseEntity<List<DemandeCongeResponse>> mesAbsences() {
        return ResponseEntity.ok(demandeCongeService.mesAbsences());
    }

    @GetMapping("/absences/stats")
    public ResponseEntity<List<AbsenceStatsResponse>> statsAbsences(@RequestParam int year) {
        return ResponseEntity.ok(demandeCongeService.getMesAbsencesStats(year));
    }

    @GetMapping("/demandes/{id}")
    public ResponseEntity<DemandeCongeResponse> getMaDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCongeService.getMaDemande(id));
    }

    @PostMapping("/demandes")
    public ResponseEntity<DemandeCongeResponse> creerBrouillon(@RequestBody DemandeCongeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandeCongeService.creerBrouillon(request));
    }

    @PutMapping("/demandes/{id}")
    public ResponseEntity<DemandeCongeResponse> modifierDemande(
            @PathVariable Long id,
            @RequestBody DemandeCongeUpdateRequest request) {
        return ResponseEntity.ok(demandeCongeService.modifierDemande(id, request));
    }

    @PostMapping("/demandes/{id}/submit")
    public ResponseEntity<DemandeCongeResponse> submitDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCongeService.submitDemande(id));
    }

    @PostMapping("/demandes/{id}/modifier")
    public ResponseEntity<DemandeCongeResponse> passerEnModification(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCongeService.passerEnModification(id));
    }

    @PostMapping("/demandes/{id}/annuler")
    public ResponseEntity<DemandeCongeResponse> annulerDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCongeService.annulerDemande(id));
    }

    @GetMapping("/solde-conge")
    public ResponseEntity<SoldeCongeResponse> getSoldeConge() {
        return ResponseEntity.ok(demandeCongeService.getSoldeCourant());
    }

    @GetMapping("/demandes/calcul-jours")
    public ResponseEntity<Map<String, Double>> calculerJours(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) {
        return ResponseEntity.ok(Map.of("jours", soldeCongeService.calculerJoursCongeOuvres(dateDebut, dateFin)));
    }
}
