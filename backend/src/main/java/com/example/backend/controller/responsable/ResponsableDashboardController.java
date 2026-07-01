package com.example.backend.controller.responsable;

import com.example.backend.dto.dashboard.ResponsableDashboardResponse;
import com.example.backend.service.interfaces.IDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/responsable/dashboard")
public class ResponsableDashboardController {

    private final IDashboardService dashboardService;

    public ResponsableDashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ResponsableDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getResponsableDashboard());
    }
}
