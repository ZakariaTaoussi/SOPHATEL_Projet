package com.example.backend.controller.admin;

import com.example.backend.dto.admin.AgendaResponse;
import com.example.backend.dto.admin.CreateAgendaRequest;
import com.example.backend.dto.admin.JourCalendrierResponse;
import com.example.backend.service.interfaces.IGestionAgenda;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/agendas")
public class AgendaController {

    private final IGestionAgenda gestionAgenda;

    public AgendaController(IGestionAgenda gestionAgenda) {
        this.gestionAgenda = gestionAgenda;
    }

    @PostMapping
    public ResponseEntity<AgendaResponse> creerAgenda(@RequestBody CreateAgendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestionAgenda.creerAgenda(request.getAnnee()));
    }

    @GetMapping
    public ResponseEntity<List<AgendaResponse>> consulterAgendas() {
        return ResponseEntity.ok(gestionAgenda.consulterAgendas());
    }

    @GetMapping("/annee/{annee}")
    public ResponseEntity<AgendaResponse> consulterAgendaParAnnee(@PathVariable Integer annee) {
        return ResponseEntity.ok(gestionAgenda.consulterAgendaParAnnee(annee));
    }

    @GetMapping("/{annee}/jours")
    public ResponseEntity<List<JourCalendrierResponse>> consulterJoursCalendrier(@PathVariable Integer annee) {
        return ResponseEntity.ok(gestionAgenda.consulterJoursCalendrier(annee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAgenda(@PathVariable Long id) {
        gestionAgenda.supprimerAgenda(id);
        return ResponseEntity.noContent().build();
    }
}
