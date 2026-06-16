package com.systemankiet.dto;

import lombok.Data;

import java.util.List;

/**
 * Statystyki roczne dla panelu admina — liczba nowych użytkowników i utworzonych ankiet
 * w każdym z ostatnich 12 miesięcy (włącznie z bieżącym).
 * Zwracane przez GET /api/admin/statistics.
 */
@Data
public class AdminStatisticsDto {

    private List<MonthlyStatDto> userStats;
    private List<MonthlyStatDto> surveyStats;
}
