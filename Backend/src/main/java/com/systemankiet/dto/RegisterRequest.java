package com.systemankiet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Żądanie rejestracji przesyłane przez frontend do POST /api/auth/register.
 * Hasło i confirmPassword wykluczone z toString/equals ze względów bezpieczeństwa.
 * Backend wyciąga domenę z emaila automatycznie (AuthService) i przypisuje rolę USER.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "confirmPassword"})
@EqualsAndHashCode(exclude = {"password", "confirmPassword"})
public class RegisterRequest {

    private String firstName;
    private String lastName;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidlowy format email")
    private String email;

    @NotBlank(message = "Haslo jest wymagane")
    @Size(min = 8, message = "Haslo musi miec co najmniej 8 znakow")
    private String password;

    // Pole opcjonalne – walidacja zgodności haseł odbywa się po stronie frontendu.
    // Backend sprawdza tylko gdy pole jest obecne w żądaniu.
    private String confirmPassword;
}
