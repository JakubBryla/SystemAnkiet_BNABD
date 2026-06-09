package com.systemankiet.service;

import com.systemankiet.dto.AuthResponse;
import com.systemankiet.dto.LoginRequest;
import com.systemankiet.dto.RegisterRequest;
import com.systemankiet.entity.User;
import com.systemankiet.enums.Role;
import com.systemankiet.repository.UserRepository;
import com.systemankiet.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Uzytkownik nie znaleziony"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public void register(RegisterRequest request) {
        if (request.getConfirmPassword() != null
                && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Hasla nie sa takie same");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email jest juz zajety");
        }

        String email = request.getEmail().trim().toLowerCase();
        String domain = email.contains("@") ? email.substring(email.indexOf("@") + 1) : null;

        User user = User.builder()
            .email(email)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .domain(domain)
            .build();

        userRepository.save(user);
    }
}
