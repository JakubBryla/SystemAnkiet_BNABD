package com.systemankiet.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Uruchamia się PRZED DataInitializer (@Order(0) < domyślny Integer.MAX_VALUE).
 * 1. Usuwa stary CHECK constraint na kolumnie 'role' (ograniczał do USER/ADMIN).
 * 2. Tworzy filtrowany indeks unikalny na (survey_id, respondent_id) WHERE NOT NULL
 *    — gwarantuje atomowość ochrony przed wielokrotnym wypełnieniem ankiety (race condition).
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    // Dozwolone znaki w nazwach SQL Server — chroni przed malformed DDL
    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9_\\-\\.]+$");

    @Override
    public void run(ApplicationArguments args) {
        dropRoleCheckConstraints();
        addLastActivatedAtColumn();
        dropUniqueResponseIndex();
    }

    private void dropRoleCheckConstraints() {
        try {
            List<String> constraints = jdbcTemplate.queryForList(
                    "SELECT cc.name " +
                    "FROM sys.check_constraints cc " +
                    "INNER JOIN sys.columns c " +
                    "  ON cc.parent_column_id = c.column_id " +
                    " AND cc.parent_object_id = c.object_id " +
                    "WHERE OBJECT_NAME(cc.parent_object_id) = 'users' " +
                    "  AND c.name = 'role'",
                    String.class
            );

            for (String name : constraints) {
                // Walidacja nazwy przed użyciem w DDL — ochrona przed malformed SQL
                if (!SAFE_NAME.matcher(name).matches()) {
                    log.warn("Pominięto constraint o nieprawidłowej nazwie: '{}'", name);
                    continue;
                }
                jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT [" + name + "]");
                log.info("=== Usunięto CHECK constraint na kolumnie role: {} ===", name);
            }

            if (constraints.isEmpty()) {
                log.debug("Brak CHECK constraints na kolumnie role — nic do usunięcia.");
            }
        } catch (Exception e) {
            log.warn("Nie można przetworzyć CHECK constraints na kolumnie role: {}", e.getMessage());
        }
    }

    /**
     * Dodaje kolumnę last_activated_at do tabeli surveys (jeśli jeszcze nie istnieje).
     * Istniejące ankiety ACTIVE dostają wartość created_at jako rozsądny default —
     * oznacza to że ich "okres aktywności" liczymy od momentu utworzenia,
     * co zabezpiecza przed ponownym wypełnieniem przez tych samych użytkowników.
     */
    private void addLastActivatedAtColumn() {
        try {
            List<Integer> exists = jdbcTemplate.queryForList(
                    "SELECT 1 FROM sys.columns " +
                    "WHERE object_id = OBJECT_ID('surveys') AND name = 'last_activated_at'",
                    Integer.class
            );
            if (exists.isEmpty()) {
                jdbcTemplate.execute(
                        "ALTER TABLE surveys ADD last_activated_at DATETIME2 NULL"
                );
                // Ustaw rozsądny default dla istniejących aktywnych ankiet
                jdbcTemplate.execute(
                        "UPDATE surveys SET last_activated_at = created_at WHERE status = 'ACTIVE'"
                );
                log.info("=== Dodano kolumnę last_activated_at do tabeli surveys ===");
            } else {
                log.debug("Kolumna last_activated_at już istnieje — pomijam.");
            }
        } catch (Exception e) {
            log.warn("Nie można dodać kolumny last_activated_at: {}", e.getMessage());
        }
    }

    /**
     * Usuwa filtrowany indeks unikalny (survey_id, respondent_id) jeśli istnieje.
     * Indeks był potrzebny gdy każdy użytkownik mógł wypełnić ankietę tylko raz globalnie.
     * Po wprowadzeniu lastActivatedAt duplikaty są kontrolowane aplikacyjnie per-okres-aktywności,
     * więc unikalny indeks blokował by poprawne ponowne wypełnienia po ponownym otwarciu ankiety.
     */
    private void dropUniqueResponseIndex() {
        try {
            String indexName = "UQ_survey_responses_survey_respondent";
            List<Integer> exists = jdbcTemplate.queryForList(
                    "SELECT 1 FROM sys.indexes " +
                    "WHERE name = ? AND object_id = OBJECT_ID('survey_responses')",
                    Integer.class, indexName
            );
            if (!exists.isEmpty()) {
                jdbcTemplate.execute(
                        "DROP INDEX [" + indexName + "] ON survey_responses"
                );
                log.info("=== Usunięto indeks unikalny {} (zastąpiony kontrolą per-okres-aktywności) ===", indexName);
            } else {
                log.debug("Indeks {} nie istnieje — pomijam.", indexName);
            }
        } catch (Exception e) {
            log.warn("Nie można usunąć indeksu unikalnego na survey_responses: {}", e.getMessage());
        }
    }
}
