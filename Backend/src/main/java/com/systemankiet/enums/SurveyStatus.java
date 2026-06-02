package com.systemankiet.enums;

public enum SurveyStatus {
    SZKIC,
    AKTYWNA,
    ZAKONCZONA;

    public String getDisplayName() {
        return switch (this) {
            case AKTYWNA -> "Aktywna";
            case ZAKONCZONA -> "Zakończona";
            default -> "Szkic";
        };
    }
}
