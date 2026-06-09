package com.systemankiet.dto;

import com.systemankiet.entity.SurveyResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseDto {

    private Long id;
    private Long surveyId;
    private LocalDateTime submittedAt;
    private boolean flagged;
    private String flagReason;
    private String flagStatus;

    public static ResponseDto fromEntity(SurveyResponse response) {
        ResponseDto dto = new ResponseDto();
        dto.setId(response.getId());
        dto.setSurveyId(response.getSurvey().getId());
        dto.setSubmittedAt(response.getSubmittedAt());
        dto.setFlagged(response.isFlagged());
        dto.setFlagReason(response.getFlagReason());
        dto.setFlagStatus(response.getFlagStatus());
        return dto;
    }
}
