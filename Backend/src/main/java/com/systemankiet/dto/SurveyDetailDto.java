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
    private String type;
    private List<QuestionDto> questions;
    // Czy ankieta ma już zapisane odpowiedzi — pytania są wtedy zablokowane do edycji
    private boolean hasResponses;
    private long responseCount;

    public static SurveyDetailDto fromEntity(Survey survey) {
        SurveyDetailDto dto = new SurveyDetailDto();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setStatus(survey.getStatus().getDisplayName());
        dto.setType(survey.getType() != null ? survey.getType().getDisplayName() : "external");
        dto.setQuestions(
            survey.getQuestions().stream()
                .map(QuestionDto::fromEntity)
                .collect(Collectors.toList())
        );
        return dto;
    }
}
