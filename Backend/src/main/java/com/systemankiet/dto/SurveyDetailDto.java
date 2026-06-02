package com.systemankiet.dto;

import com.systemankiet.entity.Survey;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pelny widok ankiety zawierajacy pytania i opcje odpowiedzi.
 * Uzywany przez publiczny endpoint dla SurveyFiller (/api/surveys/{id}/public).
 */
@Data
public class SurveyDetailDto {

    private Long id;
    private String title;
    private String description;
    private String status;
    private List<QuestionDto> questions;

    public static SurveyDetailDto fromEntity(Survey survey) {
        SurveyDetailDto dto = new SurveyDetailDto();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setStatus(survey.getStatus().getDisplayName());
        dto.setQuestions(
            survey.getQuestions().stream()
                .map(QuestionDto::fromEntity)
                .collect(Collectors.toList())
        );
        return dto;
    }
}
