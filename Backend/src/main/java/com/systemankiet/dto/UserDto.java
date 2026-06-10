package com.systemankiet.dto;

import com.systemankiet.entity.User;
import lombok.Data;

/**
 * Dane użytkownika zwracane w panelu admina (GET /api/users).
 * Nie zawiera hasła — backend nigdy nie odsyła pola password do frontendu.
 */
@Data
public class UserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String domain;
    private boolean active;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        dto.setDomain(user.getDomain());
        dto.setActive(user.isActive());
        return dto;
    }
}
