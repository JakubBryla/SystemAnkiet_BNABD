package com.systemankiet.enums;

/**
 * Typ zdarzenia logowanego do tabeli event_log — podstawa statystyk panelu admina.
 * Log jest append-only (nigdy nie usuwany), więc usunięcie użytkownika lub ankiety
 * nie wpływa retroaktywnie na wcześniej naliczone statystyki.
 */
public enum EventType {
    USER_REGISTERED,
    SURVEY_CREATED
}
