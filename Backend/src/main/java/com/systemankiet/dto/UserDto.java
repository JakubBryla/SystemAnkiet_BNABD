package com.systemankiet.dto;

import com.systemankiet.entity.User;
import lombok.Data;

@Data
public class UserDto {

    private Long id;
    private String email;
    private String role;
    private String domain;
    private boolean active;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setDomain(user.getDomain());
        dto.setActive(user.isActive());
        return dto;
    }
}
