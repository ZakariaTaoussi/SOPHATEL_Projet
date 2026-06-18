package com.example.backend.controller.responsable;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.dto.demande.ResponsableValidationDemandeRequest;
import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.service.interfaces.IResponsableDemandeService;
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
@RequestMapping("/api/responsable")
public class ResponsableDemandeController {

    private final IResponsableDemandeService responsableDemandeService;
    private final ISelfDemandeCongeService selfDemandeCongeService;
    private final ISoldeCongeService soldeCongeService;

    public ResponsableDemandeController(
            IResponsableDemandeService responsableDemandeService,
            ISelfDemandeCongeService selfDemandeCongeService,
            ISoldeCongeService soldeCongeService) {
        this.responsableDemandeService = responsableDemandeService;
        this.selfDemandeCongeService = selfDemandeCongeService;
        this.soldeCongeService = soldeCongeService;
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

    @GetMapping("/mes-demandes")
    public ResponseEntity<List<DemandeCongeResponse>> mesDemandes() {
        return ResponseEntity.ok(selfDemandeCongeService.mesDemandes());
    }

    @GetMapping("/mes-demandes/{id}")
    public ResponseEntity<DemandeCongeResponse> getMaDemande(@PathVariable Long id) {
        return ResponseEntity.ok(selfDemandeCongeService.getMaDemande(id));
    }

    @PostMapping("/mes-demandes")
    public ResponseEntity<DemandeCongeResponse> creerBrouillon(@RequestBody DemandeCongeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(selfDemandeCongeService.creerBrouillon(request));
    }

    @PutMapping("/mes-demandes/{id}")
    public ResponseEntity<DemandeCongeResponse> modifierMaDemande(
            @PathVariable Long id,
            @RequestBody DemandeCongeUpdateRequest request) {
        return ResponseEntity.ok(selfDemandeCongeService.modifierDemande(id, request));
    }

    @PostMapping("/mes-demandes/{id}/submit")
    public ResponseEntity<DemandeCongeResponse> submitMaDemande(@PathVariable Long id) {
        return ResponseEntity.ok(selfDemandeCongeService.submitDemande(id));
    }

    @PostMapping("/mes-demandes/{id}/modifier")
    public ResponseEntity<DemandeCongeResponse> passerMaDemandeEnModification(@PathVariable Long id) {
        return ResponseEntity.ok(selfDemandeCongeService.passerEnModification(id));
    }

    @PostMapping("/mes-demandes/{id}/annuler")
    public ResponseEntity<DemandeCongeResponse> annulerMaDemande(@PathVariable Long id) {
        return ResponseEntity.ok(selfDemandeCongeService.annulerDemande(id));
    }

    @GetMapping("/solde-conge")
    public ResponseEntity<SoldeCongeResponse> getSoldeConge() {
        return ResponseEntity.ok(selfDemandeCongeService.getSoldeCourant());
    }

    @GetMapping("/mes-demandes/calcul-jours")
    public ResponseEntity<Map<String, Double>> calculerJours(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) {
        return ResponseEntity.ok(Map.of("jours", soldeCongeService.calculerJoursCongeOuvres(dateDebut, dateFin)));
    }
}
