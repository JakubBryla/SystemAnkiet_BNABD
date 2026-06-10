package com.systemankiet.dto;

import lombok.Data;

import java.util.Map;

/**
 * Żądanie wysłania wypełnienia ankiety (POST /api/surveys/{id}/responses).
 * Mapa: klucz = id pytania, wartość = odpowiedź użytkownika.
 * Dla multiple-choice wartości są łączone przecinkami w jeden string po stronie frontendu.
 */
@Data
public class SubmitResponseRequest {

    // Klucz: id pytania, wartosc: tresc odpowiedzi
    private Map<Long, String> answers;
}
