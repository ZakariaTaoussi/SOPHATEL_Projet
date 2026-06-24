package com.example.backend.controller;

import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.service.interfaces.IDemandeCongeImpressionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes")
public class DemandeCongeImpressionController {

    private final IDemandeCongeImpressionService impressionService;

    public DemandeCongeImpressionController(IDemandeCongeImpressionService impressionService) {
        this.impressionService = impressionService;
    }

    @GetMapping("/{id}/impression")
    public ResponseEntity<DemandeCongeImpressionResponse> getDemandePourImpression(@PathVariable Long id) {
        return ResponseEntity.ok(impressionService.getDemandePourImpression(id));
    }
}
