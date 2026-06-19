package com.example.backend.controller.directeurgeneral;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.directeurgeneral.DirecteurGeneralEmployeResponse;
import com.example.backend.service.interfaces.IDirecteurGeneralEmployeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directeur-general/employes")
public class DirecteurGeneralEmployeController {

    private final IDirecteurGeneralEmployeService directeurGeneralEmployeService;

    public DirecteurGeneralEmployeController(IDirecteurGeneralEmployeService directeurGeneralEmployeService) {
        this.directeurGeneralEmployeService = directeurGeneralEmployeService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<DirecteurGeneralEmployeResponse>> getEmployes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departementId) {
        return ResponseEntity.ok(directeurGeneralEmployeService.getEmployes(page, size, search, role, departementId));
    }
}
