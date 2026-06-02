package com.systemankiet.dto;

import com.systemankiet.entity.Survey;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurveyDto {

    private Long id;
    private String title;
    private String status;
    private int responses;
    private LocalDateTime createdAt;

    public static SurveyDto fromEntity(Survey survey) {
        SurveyDto dto = new SurveyDto();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setStatus(survey.getStatus().getDisplayName());
        dto.setResponses(survey.getResponsesCount());
        dto.setCreatedAt(survey.getCreatedAt());
        return dto;
    }
}
