package com.example.backend.dto.dashboard;

import java.util.List;

public record EmployeDashboardResponse(
        Double soldeActuel,
        Double soldeTotal,
        Integer anneeSolde,
        Long totalDemandesConge,
        Long totalAbsences,
        Long brouillonsCount,
        Long demandesEnAttenteResponsable,
        Long demandesValideesDg,
        Long demandesRefusees,
        Long absencesThisYear,
        Long absencesThisMonth,
        List<DashboardRecentDemandeDto> latestDemandes,
        List<DashboardNotificationDto> latestNotifications,
        Long unreadNotificationsCount) {
}
