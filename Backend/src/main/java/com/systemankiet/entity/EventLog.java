package com.systemankiet.entity;

import com.systemankiet.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Encja logu zdarzeń — tabela "event_log".
 * Append-only: wpisy są tylko dodawane, nigdy usuwane ani modyfikowane.
 * Podstawa statystyk panelu admina — w przeciwieństwie do liczenia wierszy
 * w tabelach users/surveys, usunięcie ankiety lub użytkownika nie zmienia
 * historycznych statystyk, bo log zachowuje wpis o zdarzeniu na zawsze.
 */
@Entity
@Table(name = "event_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}
