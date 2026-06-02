package com.systemankiet.dto;

import com.systemankiet.entity.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuestionDto {

    // Pola wejsciowe (przyjmowane z frontendu)
    // Nazwy pol odpowiadaja kluczom JSON: "text", "type", "options"
    @NotBlank(message = "Tresc pytania nie moze byc pusta")
    private String text;

    @NotBlank(message = "Typ pytania jest wymagany")
    private String type;

    private List<String> options = new ArrayList<>();

    // Pole wyjsciowe (zwracane do frontendu po zapisie)
    private Long id;

    public static QuestionDto fromEntity(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getQuestionText());
        dto.setType(question.getQuestionType());
        dto.setOptions(
            question.getOptions().stream()
                .map(opt -> opt.getOptionText())
                .collect(Collectors.toList())
        );
        return dto;
    }
}
