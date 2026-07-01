package com.example.backend.dto.dashboard;

import java.util.List;

public record RhDashboardResponse(
        Double soldeActuel,
        Double soldeTotal,
        Integer anneeSolde,
        Long congesValidesDgThisMonth,
        Long congesValidesDgThisYear,
        Long absencesValideesDgThisMonth,
        Long absencesValideesDgThisYear,
        Double totalJoursCongeValidesThisMonth,
        Double totalJoursAbsencePaieThisMonth,
        List<DashboardChartItemDto> employeesWithMostAbsences,
        List<DashboardChartItemDto> validatedByDepartment,
        List<DashboardRecentDemandeDto> recentCongesValidesDg,
        List<DashboardRecentDemandeDto> recentAbsencesValideesDg,
        List<DashboardNotificationDto> latestNotifications,
        Long unreadNotificationsCount) {
}
