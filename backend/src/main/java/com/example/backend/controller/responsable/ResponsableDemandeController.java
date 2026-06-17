package com.example.backend.controller.responsable;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.dto.demande.ResponsableValidationDemandeRequest;
import com.example.backend.service.interfaces.IResponsableDemandeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responsable")
public class ResponsableDemandeController {

    private final IResponsableDemandeService responsableDemandeService;

    public ResponsableDemandeController(IResponsableDemandeService responsableDemandeService) {
        this.responsableDemandeService = responsableDemandeService;
    }

    @GetMapping("/demandes-a-valider")
    public ResponseEntity<List<ResponsableDemandeResponse>> demandesAValider() {
        return ResponseEntity.ok(responsableDemandeService.getDemandesAValider());
    }

    @GetMapping("/demandes-a-valider/{id}")
    public ResponseEntity<ResponsableDemandeResponse> demandeAValider(@PathVariable Long id) {
        return ResponseEntity.ok(responsableDemandeService.getDemandeAValiderById(id));
    }

    @PostMapping("/demandes/{id}/valider")
    public ResponseEntity<ResponsableDemandeResponse> validerDemande(
            @PathVariable Long id,
            @RequestBody(required = false) ResponsableValidationDemandeRequest request) {
        return ResponseEntity.ok(responsableDemandeService.validerDemandeParResponsable(id, request));
    }

    @PostMapping("/demandes/{id}/refuser")
    public ResponseEntity<ResponsableDemandeResponse> refuserDemande(@PathVariable Long id) {
        return ResponseEntity.ok(responsableDemandeService.refuserDemandeParResponsable(id));
    }

    @PostMapping({"/demandes/{id}/modifier", "/demandes/{id}/demander-modification"})
    public ResponseEntity<ResponsableDemandeResponse> demanderModification(@PathVariable Long id) {
        return ResponseEntity.ok(responsableDemandeService.passerEnModificationResponsable(id));
    }
}
