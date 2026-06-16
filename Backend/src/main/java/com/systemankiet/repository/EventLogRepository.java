package com.systemankiet.repository;

import com.systemankiet.entity.EventLog;
import com.systemankiet.enums.EventType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium logu zdarzeń — append-only.
 * Celowo rozszerza tylko bazowy znacznik Repository (nie JpaRepository/CrudRepository)
 * i udostępnia wyłącznie save() oraz odczyt (findOccurredAtSince) — bez delete/update,
 * żeby uniemożliwić modyfikację lub usunięcie już zapisanych zdarzeń na poziomie typu.
 */
@Repository
public interface EventLogRepository extends org.springframework.data.repository.Repository<EventLog, Long> {

    EventLog save(EventLog eventLog);

    // Daty zdarzeń danego typu od podanego momentu — używane przez StatisticsService do grupowania po miesiącach
    @Query("SELECT e.occurredAt FROM EventLog e WHERE e.eventType = :eventType AND e.occurredAt >= :since")
    List<LocalDateTime> findOccurredAtSince(@Param("eventType") EventType eventType, @Param("since") LocalDateTime since);
}
