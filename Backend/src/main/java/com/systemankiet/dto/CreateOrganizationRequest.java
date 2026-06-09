package com.systemankiet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrganizationRequest {

    @NotBlank(message = "Nazwa organizacji nie moze byc pusta")
    private String name;
}
