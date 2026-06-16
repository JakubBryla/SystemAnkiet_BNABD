package com.systemankiet.service;

import com.systemankiet.dto.AdminStatisticsDto;
import com.systemankiet.dto.MonthlyStatDto;
import com.systemankiet.repository.SurveyRepository;
import com.systemankiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serwis statystyk dla panelu admina.
 * Liczy nowych użytkowników i utworzone ankiety w podziale na miesiące
 * dla ostatnich 12 miesięcy (włącznie z bieżącym).
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private static final int MONTHS_BACK = 12;

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    public AdminStatisticsDto getYearlyStatistics() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(MONTHS_BACK - 1);
        LocalDateTime since = startMonth.atDay(1).atStartOfDay();

        List<LocalDateTime> userDates = userRepository.findCreatedAtSince(since);
        List<LocalDateTime> surveyDates = surveyRepository.findCreatedAtSince(since);

        AdminStatisticsDto dto = new AdminStatisticsDto();
        dto.setUserStats(buildMonthlyStats(userDates, startMonth));
        dto.setSurveyStats(buildMonthlyStats(surveyDates, startMonth));
        return dto;
    }

    // Grupuje daty po miesiącu i wypełnia brakujące miesiące zerami —
    // zawsze zwraca dokładnie 12 elementów w kolejności chronologicznej (najstarszy → bieżący).
    private List<MonthlyStatDto> buildMonthlyStats(List<LocalDateTime> dates, YearMonth startMonth) {
        Map<YearMonth, Long> counts = dates.stream()
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));

        List<MonthlyStatDto> result = new ArrayList<>();
        for (int i = 0; i < MONTHS_BACK; i++) {
            YearMonth month = startMonth.plusMonths(i);
            String label = "%02d/%d".formatted(month.getMonthValue(), month.getYear());
            result.add(new MonthlyStatDto(label, counts.getOrDefault(month, 0L)));
        }
        return result;
    }
}
