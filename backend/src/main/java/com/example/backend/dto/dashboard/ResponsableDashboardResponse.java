package com.example.backend.dto.dashboard;

import java.util.List;

public record ResponsableDashboardResponse(
        Double soldeActuel,
        Double soldeTotal,
        Integer anneeSolde,
        Long teamMembersCount,
        Long pendingTeamConges,
        Long pendingTeamAbsences,
        Long processedByResponsableThisMonth,
        Long refusedByResponsableThisMonth,
        Long validatedByDgForTeamThisMonth,
        List<DashboardChartItemDto> teamRequestsByStatus,
        List<DashboardRecentDemandeDto> upcomingApprovedLeaves,
        List<DashboardRecentDemandeDto> latestTeamRequests,
        List<DashboardNotificationDto> latestNotifications,
        Long unreadNotificationsCount) {
}
