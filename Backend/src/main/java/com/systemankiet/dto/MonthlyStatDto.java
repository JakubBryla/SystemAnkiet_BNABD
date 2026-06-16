package com.systemankiet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Liczba zdarzeń (nowych użytkowników lub utworzonych ankiet) w jednym miesiącu.
 * Element listy w AdminStatisticsDto — jeden słupek na wykresie w panelu admina.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatDto {

    private String label; // format MM/yyyy, np. "06/2026"
    private long count;
}
