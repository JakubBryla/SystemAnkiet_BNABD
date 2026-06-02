package com.systemankiet.dto;

import com.systemankiet.entity.Survey;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurveyDto {

    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public static SurveyDto fromEntity(Survey survey) {
        SurveyDto dto = new SurveyDto();
        dto.setId(survey.getId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setStatus(survey.getStatus().getDisplayName());
        dto.setCreatedAt(survey.getCreatedAt());
        return dto;
    }
}
