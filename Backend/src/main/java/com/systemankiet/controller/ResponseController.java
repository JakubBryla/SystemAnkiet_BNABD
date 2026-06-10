package com.systemankiet.controller;

import com.systemankiet.dto.ResponseDetailDto;
import com.systemankiet.dto.ResponseDto;
import com.systemankiet.dto.SubmitResponseRequest;
import com.systemankiet.entity.User;
import com.systemankiet.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler odpowiedzi na ankiety.
 * GET /api/surveys/{id}/responses — lista wypełnień dla ankietera (wymaga SURVEYOR/ADMIN).
 * POST /api/surveys/{id}/responses — wysyłanie wypełnienia (publiczne dla EXTERNAL, wymaga logowania dla INTERNAL).
 */
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class ResponseController {

    private final ResponseService responseService;

    // Pobieranie odpowiedzi ankiety - tylko twórca (SURVEYOR/ADMIN)
    @GetMapping("/{id}/responses")
    @PreAuthorize("hasAnyRole('SURVEYOR', 'ADMIN')")
    public ResponseEntity<List<ResponseDetailDto>> getResponses(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(responseService.getResponses(id, currentUser));
    }

    // Wysylanie odpowiedzi na ankiete.
    // Dla ankiet EXTERNAL - dostepne bez logowania.
    // Dla ankiet INTERNAL - wymagane zalogowanie i ta sama organizacja.
    @PostMapping("/{id}/responses")
    public ResponseEntity<ResponseDto> submitResponse(
            @PathVariable Long id,
            @RequestBody SubmitResponseRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseService.submitResponse(id, request, currentUser));
    }
}
