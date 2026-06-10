package com.systemankiet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Żądanie tworzenia lub edycji ankiety (POST /api/surveys i PUT /api/surveys/{id}).
 * Zawiera pełną strukturę ankiety: tytuł, opis, typ i listę pytań z opcjami.
 */
@Data
public class CreateSurveyRequest {

    @NotBlank(message = "Tytul ankiety nie moze byc pusty")
    private String title;

    private String description;

    // "internal" lub "external" (domyslnie external jesli nie podano)
    private String type;

    @Valid
    private List<QuestionDto> questions = new ArrayList<>();
}
