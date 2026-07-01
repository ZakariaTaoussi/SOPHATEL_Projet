package com.example.backend.controller.directeurgeneral;

import com.example.backend.dto.dashboard.DirecteurGeneralDashboardResponse;
import com.example.backend.service.interfaces.IDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/directeur-general/dashboard")
public class DirecteurGeneralDashboardController {

    private final IDashboardService dashboardService;

    public DirecteurGeneralDashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DirecteurGeneralDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDirecteurGeneralDashboard());
    }
}
