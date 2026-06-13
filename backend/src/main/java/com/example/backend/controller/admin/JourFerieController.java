package com.example.backend.controller.admin;

import com.example.backend.dto.admin.CreateJourFerieRequest;
import com.example.backend.dto.admin.JourFerieResponse;
import com.example.backend.dto.admin.UpdateJourFerieRequest;
import com.example.backend.service.interfaces.IGestionJourFerie;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jours-feries")
public class JourFerieController {

    private final IGestionJourFerie gestionJourFerie;

    public JourFerieController(IGestionJourFerie gestionJourFerie) {
        this.gestionJourFerie = gestionJourFerie;
    }

    @PostMapping
    public ResponseEntity<JourFerieResponse> creerJourFerie(@RequestBody CreateJourFerieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestionJourFerie.creerJourFerie(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JourFerieResponse> modifierJourFerie(@PathVariable Long id, @RequestBody UpdateJourFerieRequest request) {
        return ResponseEntity.ok(gestionJourFerie.modifierJourFerie(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerJourFerie(@PathVariable Long id) {
        gestionJourFerie.supprimerJourFerie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<JourFerieResponse>> consulterJoursFeriesParAnnee(@RequestParam Integer annee) {
        return ResponseEntity.ok(gestionJourFerie.consulterJoursFeriesParAnnee(annee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JourFerieResponse> consulterJourFerie(@PathVariable Long id) {
        return ResponseEntity.ok(gestionJourFerie.consulterJourFerie(id));
    }
}
