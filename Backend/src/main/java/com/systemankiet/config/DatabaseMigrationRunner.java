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
        createUniqueResponseIndex();
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
     * Tworzy filtrowany indeks unikalny (survey_id, respondent_id) WHERE respondent_id IS NOT NULL.
     * Zapobiega race condition przy równoczesnych żądaniach wypełnienia tej samej ankiety.
     * Ankiety EXTERNAL (respondent_id = NULL) nie są objęte constraintem — wiele odpowiedzi anonimowych jest OK.
     */
    private void createUniqueResponseIndex() {
        try {
            String indexName = "UQ_survey_responses_survey_respondent";
            List<Integer> exists = jdbcTemplate.queryForList(
                    "SELECT 1 FROM sys.indexes " +
                    "WHERE name = ? AND object_id = OBJECT_ID('survey_responses')",
                    Integer.class, indexName
            );

            if (exists.isEmpty()) {
                jdbcTemplate.execute(
                        "CREATE UNIQUE INDEX " + indexName +
                        " ON survey_responses(survey_id, respondent_id)" +
                        " WHERE respondent_id IS NOT NULL"
                );
                log.info("=== Utworzono filtrowany indeks unikalny: {} ===", indexName);
            } else {
                log.debug("Indeks {} już istnieje — pomijam.", indexName);
            }
        } catch (Exception e) {
            log.warn("Nie można utworzyć indeksu unikalnego na survey_responses: {}", e.getMessage());
        }
    }
}
