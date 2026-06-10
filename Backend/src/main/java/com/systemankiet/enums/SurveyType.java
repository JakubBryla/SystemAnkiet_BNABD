package com.systemankiet.enums;

/**
 * Typ ankiety określający grupę docelową i wymagania dostępu.
 * INTERNAL — dostępna tylko dla zalogowanych użytkowników z tej samej domeny emaila co twórca.
 * EXTERNAL — publiczna, dostępna przez link bez logowania (np. dla klientów).
 */
public enum SurveyType {
    INTERNAL,
    EXTERNAL;

    public String getDisplayName() {
        return this.name().toLowerCase();
    }
}
