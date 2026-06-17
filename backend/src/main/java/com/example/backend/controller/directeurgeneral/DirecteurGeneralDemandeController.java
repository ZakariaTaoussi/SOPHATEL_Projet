package com.example.backend.controller.directeurgeneral;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.service.interfaces.IDirecteurGeneralDemandeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<List<ResponsableDemandeResponse>> demandesAValider() {
        return ResponseEntity.ok(directeurGeneralDemandeService.getDemandesAValider());
    }
}
