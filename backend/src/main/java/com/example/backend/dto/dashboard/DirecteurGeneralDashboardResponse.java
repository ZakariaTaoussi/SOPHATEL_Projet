package com.example.backend.dto.dashboard;

import java.util.List;

public record DirecteurGeneralDashboardResponse(
        Long pendingCongesForDg,
        Long pendingAbsencesForDg,
        Long validatedByDgThisMonth,
        Long refusedByDgThisMonth,
        Long modifiedByDgThisMonth,
        Long totalEmployeesExceptAdmins,
        List<DashboardChartItemDto> employeesByDepartment,
        List<DashboardChartItemDto> employeesByRole,
        List<DashboardRecentDemandeDto> recentResponsableValidatedRequests,
        List<DashboardRecentDemandeDto> recentDgProcessedRequests,
        List<DashboardNotificationDto> latestNotifications,
        Long unreadNotificationsCount) {
}
