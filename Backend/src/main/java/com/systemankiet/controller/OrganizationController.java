package com.systemankiet.controller;

import com.systemankiet.dto.CreateOrganizationRequest;
import com.systemankiet.dto.OrganizationDto;
import com.systemankiet.entity.User;
import com.systemankiet.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // Tworzenie nowej organizacji
    @PostMapping
    public ResponseEntity<OrganizationDto> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request));
    }

    // Lista wszystkich organizacji (np. do wyboru przy dolaczaniu)
    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    // Dolaczenie zalogowanego uzytkownika do wybranej organizacji
    @PatchMapping("/join/{organizationId}")
    public ResponseEntity<Void> joinOrganization(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal User user) {
        organizationService.joinOrganization(organizationId, user);
        return ResponseEntity.noContent().build();
    }
}
