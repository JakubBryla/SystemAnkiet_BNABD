package com.systemankiet.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Uruchamia się PRZED DataInitializer (@Order(0) < domyślny Integer.MAX_VALUE).
 * Usuwa stary CHECK constraint na kolumnie 'role', który ograniczał wartości
 * tylko do USER i ADMIN — po dodaniu roli SURVEYOR constraint musi zostać zaktualizowany.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        dropRoleCheckConstraints();
    }

    private void dropRoleCheckConstraints() {
        try {
            // Znajdź wszystkie CHECK constraints na kolumnie 'role' w tabeli 'users'
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
}
