package com.example.backend.controller.responsable;

import com.example.backend.dto.profil.ProfilResponse;
import com.example.backend.dto.profil.ProfilUpdateRequest;
import com.example.backend.service.interfaces.IProfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responsable/profil")
public class ResponsableProfilController {

    private final IProfilService profilService;

    public ResponsableProfilController(IProfilService profilService) {
        this.profilService = profilService;
    }

    @GetMapping
    public ResponseEntity<ProfilResponse> getProfil() {
        return ResponseEntity.ok(profilService.getProfilConnecte());
    }

    @PutMapping
    public ResponseEntity<ProfilResponse> updateProfil(@RequestBody ProfilUpdateRequest request) {
        return ResponseEntity.ok(profilService.updateProfilConnecte(request));
    }
}
