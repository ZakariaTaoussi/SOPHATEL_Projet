package com.example.backend.controller.admin;

import com.example.backend.dto.admin.CreateDepartementRequest;
import com.example.backend.dto.admin.DepartementResponse;
import com.example.backend.dto.admin.UpdateDepartementRequest;
import com.example.backend.service.interfaces.IGestionDepartement;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/departements")
public class DepartementController {

    private final IGestionDepartement gestionDepartement;

    public DepartementController(IGestionDepartement gestionDepartement) {
        this.gestionDepartement = gestionDepartement;
    }

    @PostMapping
    public ResponseEntity<DepartementResponse> creerDepartement(@RequestBody CreateDepartementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestionDepartement.creerDepartement(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartementResponse> modifierDepartement(@PathVariable Long id, @RequestBody UpdateDepartementRequest request) {
        return ResponseEntity.ok(gestionDepartement.modifierDepartement(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable Long id) {
        gestionDepartement.supprimerDepartement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DepartementResponse>> consulterDepartements() {
        return ResponseEntity.ok(gestionDepartement.consulterDepartements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartementResponse> consulterDepartement(@PathVariable Long id) {
        return ResponseEntity.ok(gestionDepartement.consulterDepartement(id));
    }
}
