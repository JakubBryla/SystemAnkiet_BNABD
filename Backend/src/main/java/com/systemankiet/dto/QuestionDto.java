package com.systemankiet.dto;

import com.systemankiet.entity.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO pytania — używane w dwóch kierunkach:
 * - z backendu do frontendu jako część SurveyDetailDto (dla wypełniającego i edytującego),
 * - z frontendu do backendu jako część CreateSurveyRequest (przy tworzeniu/edycji ankiety).
 * Opcje przechowywane jako lista stringów (nie jako obiekty AnswerOption).
 */
@Data
public class QuestionDto {

    @NotBlank(message = "Tresc pytania nie moze byc pusta")
    private String text;

    @NotBlank(message = "Typ pytania jest wymagany")
    private String type;

    private Boolean isRequired;

    private List<String> options = new ArrayList<>();

    private Long id;

    // Pola pytania kontrolnego
    private Boolean isControlQuestion;
    private String expectedValue;  // oczekiwana poprawna odpowiedź

    public static QuestionDto fromEntity(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getQuestionText());
        dto.setType(question.getQuestionType());
        dto.setIsRequired(question.isRequired());
        dto.setIsControlQuestion(question.isControlQuestion());
        dto.setExpectedValue(question.getExpectedValue());
        dto.setOptions(
            question.getOptions().stream()
                .map(opt -> opt.getOptionText())
                .collect(Collectors.toList())
        );
        return dto;
    }
}
