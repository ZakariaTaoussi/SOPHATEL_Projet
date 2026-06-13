package com.example.backend.controller.admin;

import com.example.backend.dto.admin.CreateEmployeRequest;
import com.example.backend.dto.admin.EmployeResponse;
import com.example.backend.dto.admin.PageResponse;
import com.example.backend.dto.admin.UpdateEmployeRequest;
import com.example.backend.service.interfaces.IGestionEmploye;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/admin/employes")
public class EmployeAdminController {

    private final IGestionEmploye gestionEmploye;

    public EmployeAdminController(IGestionEmploye gestionEmploye) {
        this.gestionEmploye = gestionEmploye;
    }

    @PostMapping
    public ResponseEntity<EmployeResponse> creerEmploye(@RequestBody CreateEmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestionEmploye.creerEmploye(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeResponse> modifierEmploye(@PathVariable Long id, @RequestBody UpdateEmployeRequest request) {
        return ResponseEntity.ok(gestionEmploye.modifierEmploye(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerEmploye(@PathVariable Long id) {
        gestionEmploye.supprimerEmploye(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeResponse> consulterEmploye(@PathVariable Long id) {
        return ResponseEntity.ok(gestionEmploye.consulterEmploye(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<EmployeResponse>> consulterEmployes(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "idEmp"));
        return ResponseEntity.ok(gestionEmploye.consulterEmployes(search, pageable));
    }
}
