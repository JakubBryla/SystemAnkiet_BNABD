package com.systemankiet.controller;

import com.systemankiet.dto.CreateSurveyRequest;
import com.systemankiet.dto.SurveyDto;
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

    @GetMapping
    public ResponseEntity<List<SurveyDto>> getUserSurveys(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(surveyService.getUserSurveys(user));
    }

    @PostMapping
    public ResponseEntity<SurveyDto> createSurvey(
            @Valid @RequestBody CreateSurveyRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(surveyService.createSurvey(request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        surveyService.deleteSurvey(id, user);
        return ResponseEntity.noContent().build();
    }
}
