package com.systemankiet.dto;

import com.systemankiet.entity.Survey;
import lombok.Data;

import java.time.LocalDateTime;
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
    // Domena organizacji twórcy — używana przez frontend do weryfikacji dostępu do ankiet INTERNAL
    private String creatorDomain;
    private List<QuestionDto> questions;
    // Czy ankieta ma już zapisane odpowiedzi — pytania są wtedy zablokowane do edycji
    private boolean hasResponses;
    private long responseCount;
    // Data ostatniej aktywacji — frontend używa jej jako części klucza localStorage
    // żeby różni użytkownicy i różne okresy aktywności miały osobne wpisy
    private LocalDateTime lastActivatedAt;

    public static SurveyDetailDto fromEntity(Survey survey) {
        SurveyDetailDto dto = new SurveyDetailDto();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setStatus(survey.getStatus().getDisplayName());
        dto.setType(survey.getType() != null ? survey.getType().getDisplayName() : "external");
        dto.setCreatorDomain(survey.getCreatedBy() != null ? survey.getCreatedBy().getDomain() : null);
        dto.setQuestions(
            survey.getQuestions().stream()
                .map(QuestionDto::fromEntity)
                .collect(Collectors.toList())
        );
        dto.setLastActivatedAt(survey.getLastActivatedAt());
        return dto;
    }
}
