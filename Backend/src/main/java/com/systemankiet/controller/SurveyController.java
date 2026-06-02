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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    // Pobiera liste ankiet zalogowanego uzytkownika
    @GetMapping
    public ResponseEntity<List<SurveyDto>> getUserSurveys(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.getUserSurveys(user));
    }

    // Publiczny endpoint - zwraca pelna ankiete z pytaniami (dla SurveyFiller, bez logowania)
    @GetMapping("/{id}/public")
    public ResponseEntity<SurveyDetailDto> getPublicSurvey(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getPublicSurvey(id));
    }

    // Tworzy nowa ankiete z pytaniami i opcjami (kaskadowo)
    @PostMapping
    public ResponseEntity<SurveyDto> createSurvey(
            @Valid @RequestBody CreateSurveyRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveyService.createSurvey(request, user));
    }

    // Zmienia status ankiety (draft/active/closed)
    @PatchMapping("/{id}/status")
    public ResponseEntity<SurveyDto> updateSurveyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSurveyStatusRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.updateSurveyStatus(id, request, user));
    }

    // Usuwa ankiete (tylko wlasna)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        surveyService.deleteSurvey(id, user);
        return ResponseEntity.noContent().build();
    }
}
