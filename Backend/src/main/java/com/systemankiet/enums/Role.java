package com.systemankiet.enums;

/**
 * Role użytkowników w systemie.
 * USER     — zwykły respondent, może wypełniać ankiety wewnętrzne swojej organizacji.
 * SURVEYOR — ankieter, może tworzyć ankiety i przeglądać wyniki.
 * ADMIN    — administrator, dodatkowo zarządza użytkownikami i ich rolami.
 */
public enum Role {
    USER,
    SURVEYOR,
    ADMIN
}
