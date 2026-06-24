package com.example.backend.dto.demande;

public class AbsenceStatsResponse {
    private final int month;
    private final long totalAbsences;
    private final long totalJoursAbsence;

    public AbsenceStatsResponse(int month, long totalAbsences, long totalJoursAbsence) {
        this.month = month;
        this.totalAbsences = totalAbsences;
        this.totalJoursAbsence = totalJoursAbsence;
    }

    public int getMonth() {
        return month;
    }

    public long getTotalAbsences() {
        return totalAbsences;
    }

    public long getTotalJoursAbsence() {
        return totalJoursAbsence;
    }
}
