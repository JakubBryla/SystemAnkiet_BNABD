package com.systemankiet.enums;

/**
 * Status ankiety.
 * DRAFT  — szkic, niewidoczny dla respondentów.
 * ACTIVE — opublikowana, dostępna do wypełnienia; ustawienie tego statusu aktualizuje lastActivatedAt.
 * CLOSED — zamknięta, nie można jej wypełnić; po ponownym otwarciu (→ ACTIVE) respondenci mogą wypełnić ją jeszcze raz.
 */
public enum SurveyStatus {
    DRAFT,
    ACTIVE,
    CLOSED;

    public String getDisplayName() {
        return this.name().toLowerCase();
    }
}
