package com.systemankiet.repository;

import com.systemankiet.entity.EventLog;
import com.systemankiet.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium logu zdarzeń. Zapisy są append-only — service wywołujący save()
 * nigdy nie powinien usuwać ani aktualizować istniejących wpisów.
 */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    // Daty zdarzeń danego typu od podanego momentu — używane przez StatisticsService do grupowania po miesiącach
    @Query("SELECT e.occurredAt FROM EventLog e WHERE e.eventType = :eventType AND e.occurredAt >= :since")
    List<LocalDateTime> findOccurredAtSince(@Param("eventType") EventType eventType, @Param("since") LocalDateTime since);
}
