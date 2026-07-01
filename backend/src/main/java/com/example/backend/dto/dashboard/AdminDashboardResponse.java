package com.example.backend.dto.dashboard;

import java.util.List;

public record AdminDashboardResponse(
        Long totalEmployees,
        Long totalEmployesRole,
        Long totalResponsables,
        Long totalRh,
        Long totalDirecteursGeneraux,
        Long totalAdmins,
        Long totalDepartments,
        Long holidaysThisYear,
        Long agendasCount,
        boolean currentYearAgendaExists,
        List<DashboardRecentEmployeDto> recentlyCreatedEmployees,
        List<DashboardChartItemDto> employeesByRole,
        List<DashboardChartItemDto> employeesByDepartment) {
}
