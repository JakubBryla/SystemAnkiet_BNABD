package com.systemankiet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
