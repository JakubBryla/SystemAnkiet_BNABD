package com.systemankiet.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(exclude = "token")
@EqualsAndHashCode(exclude = "token")
public class AuthResponse {

    private String token;
    private String email;
    private String role;
}
