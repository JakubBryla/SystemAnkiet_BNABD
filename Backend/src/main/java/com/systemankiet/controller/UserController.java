package com.systemankiet.controller;

import com.systemankiet.dto.UserDto;
import com.systemankiet.entity.User;
import com.systemankiet.enums.Role;
import com.systemankiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // Lista wszystkich użytkowników — tylko dla admina
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll(Sort.by("email"))
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Ustawia konkretną rolę użytkownika: USER / ANKIETER / ADMIN — tylko dla admina
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> setRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentAdmin) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik nie znaleziony"));

        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalArgumentException("Nie można zmienić własnej roli");
        }

        String roleStr = body.get("role");
        Role newRole;
        try {
            newRole = Role.valueOf(roleStr.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Nieprawidłowa rola: " + roleStr + ". Dozwolone: USER, ANKIETER, ADMIN");
        }

        user.setRole(newRole);
        return ResponseEntity.ok(UserDto.fromEntity(userRepository.save(user)));
    }
}
