package com.systemankiet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateSurveyStatusRequest {

    @NotBlank(message = "Status nie moze byc pusty")
    @Pattern(regexp = "draft|active|closed",
             message = "Nieprawidlowy status. Dozwolone wartosci: draft, active, closed")
    private String status;
}
