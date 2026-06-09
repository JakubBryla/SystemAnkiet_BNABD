package com.systemankiet.enums;

public enum SurveyType {
    INTERNAL,
    EXTERNAL;

    public String getDisplayName() {
        return this.name().toLowerCase();
    }
}
