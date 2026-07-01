package com.example.backend.service.interfaces;

import com.example.backend.dto.dashboard.AdminDashboardResponse;
import com.example.backend.dto.dashboard.DirecteurGeneralDashboardResponse;
import com.example.backend.dto.dashboard.EmployeDashboardResponse;
import com.example.backend.dto.dashboard.ResponsableDashboardResponse;
import com.example.backend.dto.dashboard.RhDashboardResponse;

public interface IDashboardService {
    AdminDashboardResponse getAdminDashboard();

    EmployeDashboardResponse getEmployeDashboard();

    ResponsableDashboardResponse getResponsableDashboard();

    RhDashboardResponse getRhDashboard(Integer annee, Integer mois);

    DirecteurGeneralDashboardResponse getDirecteurGeneralDashboard();
}
