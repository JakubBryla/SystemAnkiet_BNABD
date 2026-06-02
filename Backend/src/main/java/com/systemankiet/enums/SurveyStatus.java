package com.systemankiet.enums;

public enum SurveyStatus {
    DRAFT,
    ACTIVE,
    CLOSED;

    public String getDisplayName() {
        return this.name().toLowerCase();
    }
}
