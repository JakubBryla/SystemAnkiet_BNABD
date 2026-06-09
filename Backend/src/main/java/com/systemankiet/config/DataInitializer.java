package com.systemankiet.config;

import com.systemankiet.entity.User;
import com.systemankiet.enums.Role;
import com.systemankiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmailIgnoreCase("admin@admin.com")) {
            User admin = User.builder()
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .domain("admin.com")
                    .build();
            userRepository.save(admin);
            log.info("=== Domyślne konto admina utworzone: admin@admin.com / admin123 ===");
        }

        if (!userRepository.existsByEmailIgnoreCase("ankieter@test.com")) {
            User ankieter = User.builder()
                    .email("ankieter@test.com")
                    .password(passwordEncoder.encode("ankieter123"))
                    .role(Role.ANKIETER)
                    .domain("test.com")
                    .build();
            userRepository.save(ankieter);
            log.info("=== Domyślne konto ankietera utworzone: ankieter@test.com / ankieter123 ===");
        }
    }
}
