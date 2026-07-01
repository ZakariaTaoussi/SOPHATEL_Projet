package com.example.backend.controller.rh;

import com.example.backend.dto.dashboard.RhDashboardResponse;
import com.example.backend.service.interfaces.IDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh/dashboard")
public class RhDashboardController {

    private final IDashboardService dashboardService;

    public RhDashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<RhDashboardResponse> getDashboard(
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois) {
        return ResponseEntity.ok(dashboardService.getRhDashboard(annee, mois));
    }
}
