package com.systemankiet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidlowy format email")
    private String email;

    @NotBlank(message = "Haslo jest wymagane")
    @Size(min = 8, message = "Haslo musi miec co najmniej 8 znakow")
    private String password;

    @NotBlank(message = "Potwierdzenie hasla jest wymagane")
    private String confirmPassword;
}
