package com.systemankiet.controller;

import com.systemankiet.dto.AuthResponse;
import com.systemankiet.dto.LoginRequest;
import com.systemankiet.dto.RegisterRequest;
import com.systemankiet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Kontroler autoryzacji — rejestracja i logowanie.
 * Endpointy publiczne (bez tokenu JWT): POST /api/auth/login, POST /api/auth/register.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
