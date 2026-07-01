package com.example.backend.controller.employe;

import com.example.backend.dto.dashboard.EmployeDashboardResponse;
import com.example.backend.service.interfaces.IDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employe/dashboard")
public class EmployeDashboardController {

    private final IDashboardService dashboardService;

    public EmployeDashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<EmployeDashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getEmployeDashboard());
    }
}
