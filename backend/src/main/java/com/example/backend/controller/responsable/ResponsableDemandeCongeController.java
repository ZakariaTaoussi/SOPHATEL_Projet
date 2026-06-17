package com.example.backend.controller.responsable;

import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.service.interfaces.IDemandeCongeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responsable")
public class ResponsableDemandeCongeController {

    private final IDemandeCongeService demandeCongeService;

    public ResponsableDemandeCongeController(IDemandeCongeService demandeCongeService) {
        this.demandeCongeService = demandeCongeService;
    }

    @GetMapping("/demandes-a-valider")
    public ResponseEntity<List<DemandeCongeResponse>> demandesAValider() {
        return ResponseEntity.ok(demandeCongeService.demandesAValiderPourResponsable());
    }
}
