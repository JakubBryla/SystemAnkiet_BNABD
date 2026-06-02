package com.systemankiet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidlowy format email")
    private String email;

    @NotBlank(message = "Haslo jest wymagane")
    private String password;
}
