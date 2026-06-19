package com.example.backend.controller.responsable;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.responsable.ResponsableEmployeResponse;
import com.example.backend.service.interfaces.IResponsableEmployeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responsable/mes-employes")
public class ResponsableEmployeController {

    private final IResponsableEmployeService responsableEmployeService;

    public ResponsableEmployeController(IResponsableEmployeService responsableEmployeService) {
        this.responsableEmployeService = responsableEmployeService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ResponsableEmployeResponse>> getMesEmployes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {
        return ResponseEntity.ok(responsableEmployeService.getMesEmployes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsableEmployeResponse> getEmploye(@PathVariable Long id) {
        return ResponseEntity.ok(responsableEmployeService.getEmployeDeMonDepartement(id));
    }
}
