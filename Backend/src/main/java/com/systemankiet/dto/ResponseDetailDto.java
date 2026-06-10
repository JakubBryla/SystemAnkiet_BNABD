package com.systemankiet.dto;

import com.systemankiet.entity.SurveyResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pełne wypełnienie ankiety widoczne w panelu wyników ankietera (GET /api/surveys/{id}/responses).
 * Zawiera metadane odpowiedzi (kto, kiedy, czy oflagowana) oraz listę odpowiedzi na każde pytanie.
 */
@Data
public class ResponseDetailDto {

    private Long id;
    private LocalDateTime submittedAt;
    private boolean flagged;
    private String flagStatus;
    private String flagReason;
    private List<AnswerDetailDto> answers;

    public static ResponseDetailDto fromEntity(SurveyResponse response) {
        ResponseDetailDto dto = new ResponseDetailDto();
        dto.setId(response.getId());
        dto.setSubmittedAt(response.getSubmittedAt());
        dto.setFlagged(response.isFlagged());
        dto.setFlagStatus(response.getFlagStatus());
        dto.setFlagReason(response.getFlagReason());
        dto.setAnswers(
            response.getAnswers().stream()
                .map(AnswerDetailDto::fromEntity)
                .collect(Collectors.toList())
        );
        return dto;
    }
}
