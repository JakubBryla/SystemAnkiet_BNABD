package com.systemankiet.controller;

import com.systemankiet.dto.AdminStatisticsDto;
import com.systemankiet.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler statystyk panelu admina — dostępny wyłącznie dla roli ADMIN.
 * GET /api/admin/statistics — liczba nowych użytkowników i utworzonych ankiet w ostatnich 12 miesiącach.
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatisticsDto> getStatistics() {
        return ResponseEntity.ok(statisticsService.getYearlyStatistics());
    }
}
