package com.example.backend.controller.directeurgeneral;

import com.example.backend.dto.demande.DirecteurGeneralDemandeResponse;
import com.example.backend.dto.demande.DirecteurGeneralValidationDemandeRequest;
import com.example.backend.service.interfaces.IDirecteurGeneralDemandeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directeur-general")
public class DirecteurGeneralDemandeController {

    private final IDirecteurGeneralDemandeService directeurGeneralDemandeService;

    public DirecteurGeneralDemandeController(IDirecteurGeneralDemandeService directeurGeneralDemandeService) {
        this.directeurGeneralDemandeService = directeurGeneralDemandeService;
    }

    @GetMapping("/demandes-a-valider")
    public ResponseEntity<List<DirecteurGeneralDemandeResponse>> demandesAValider() {
        return ResponseEntity.ok(directeurGeneralDemandeService.getDemandesAValider());
    }

    @GetMapping("/demandes-a-valider/{id}")
    public ResponseEntity<DirecteurGeneralDemandeResponse> demandeAValider(@PathVariable Long id) {
        return ResponseEntity.ok(directeurGeneralDemandeService.getDemandeAValiderById(id));
    }

    @PostMapping("/demandes/{id}/valider")
    public ResponseEntity<DirecteurGeneralDemandeResponse> validerDemande(
            @PathVariable Long id,
            @RequestBody(required = false) DirecteurGeneralValidationDemandeRequest request) {
        return ResponseEntity.ok(directeurGeneralDemandeService.validerDemandeParDg(id, request));
    }

    @PostMapping("/demandes/{id}/refuser")
    public ResponseEntity<DirecteurGeneralDemandeResponse> refuserDemande(@PathVariable Long id) {
        return ResponseEntity.ok(directeurGeneralDemandeService.refuserDemandeParDg(id));
    }

    @PostMapping("/demandes/{id}/modifier")
    public ResponseEntity<DirecteurGeneralDemandeResponse> modifierDemande(@PathVariable Long id) {
        return ResponseEntity.ok(directeurGeneralDemandeService.passerEnModificationDg(id));
    }

    @GetMapping("/demandes-validees")
    public ResponseEntity<List<DirecteurGeneralDemandeResponse>> demandesValidees() {
        return ResponseEntity.ok(directeurGeneralDemandeService.getDemandesValidees());
    }
}
