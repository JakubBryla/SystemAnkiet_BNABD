package com.systemankiet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSurveyRequest {

    @NotBlank(message = "Tytul ankiety nie moze byc pusty")
    private String title;

    private String description;
}
