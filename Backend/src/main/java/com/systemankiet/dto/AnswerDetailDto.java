package com.systemankiet.dto;

import com.systemankiet.entity.ResponseAnswer;
import lombok.Data;

/**
 * Jedna para pytanie + odpowiedź użytkownika w widoku wyników ankietera.
 * Element listy w ResponseDetailDto — reprezentuje odpowiedź na pojedyncze pytanie.
 */
@Data
public class AnswerDetailDto {

    private Long questionId;
    private String questionText;
    private String questionType;
    private String answerValue;

    public static AnswerDetailDto fromEntity(ResponseAnswer answer) {
        AnswerDetailDto dto = new AnswerDetailDto();
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setQuestionText(answer.getQuestion().getQuestionText());
        dto.setQuestionType(answer.getQuestion().getQuestionType());
        dto.setAnswerValue(answer.getAnswerValue());
        return dto;
    }
}
