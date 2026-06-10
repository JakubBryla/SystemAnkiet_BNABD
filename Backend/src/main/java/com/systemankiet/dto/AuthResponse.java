package com.systemankiet.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Odpowiedź backendu po udanym logowaniu lub rejestracji.
 * Zawiera token JWT (zapisywany w localStorage przez frontend), email i rolę użytkownika.
 * Token jest wykluczony z toString/equals ze względów bezpieczeństwa.
 */
@Getter
@AllArgsConstructor
@ToString(exclude = "token")
@EqualsAndHashCode(exclude = "token")
public class AuthResponse {

    private String token;
    private String email;
    private String role;
}
