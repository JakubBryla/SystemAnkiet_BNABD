package com.systemankiet.controller;

import com.systemankiet.dto.CreateSurveyRequest;
import com.systemankiet.dto.SurveyDetailDto;
import com.systemankiet.dto.SurveyDto;
import com.systemankiet.dto.UpdateSurveyStatusRequest;
import com.systemankiet.entity.User;
import com.systemankiet.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    // Ankiety stworzone przez zalogowanego uzytkownika — tylko SURVEYOR i ADMIN mogą tworzyć ankiety
    @GetMapping
    @PreAuthorize("hasAnyRole('SURVEYOR', 'ADMIN')")
    public ResponseEntity<List<SurveyDto>> getUserSurveys(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.getUserSurveys(user));
    }

    // Ankiety wewnetrzne przypisane do uzytkownika — dostępne dla wszystkich zalogowanych
    @GetMapping("/assigned")
    public ResponseEntity<List<SurveyDto>> getAssignedSurveys(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.getAssignedSurveys(user));
    }

    // Publiczny endpoint - zwraca pelna ankiete z pytaniami (dla SurveyFiller)
    @GetMapping("/{id}/public")
    public ResponseEntity<SurveyDetailDto> getPublicSurvey(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getPublicSurvey(id));
    }

    // Tworzy nowa ankiete — tylko SURVEYOR i ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('SURVEYOR', 'ADMIN')")
    public ResponseEntity<SurveyDto> createSurvey(
            @Valid @RequestBody CreateSurveyRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveyService.createSurvey(request, user));
    }

    // Aktualizuje tresc ankiety — tylko SURVEYOR i ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SURVEYOR', 'ADMIN')")
    public ResponseEntity<SurveyDto> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody CreateSurveyRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.updateSurvey(id, request, user));
    }

    // Zmienia status ankiety — tylko SURVEYOR i ADMIN
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SURVEYOR', 'ADMIN')")
    public ResponseEntity<SurveyDto> updateSurveyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSurveyStatusRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.updateSurveyStatus(id, request, user));
    }

    // Usuwa ankiete — tylko SURVEYOR i ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SURVEYOR', 'ADMIN')")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        surveyService.deleteSurvey(id, user);
        return ResponseEntity.noContent().build();
    }
}
