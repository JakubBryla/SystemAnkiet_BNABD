package com.systemankiet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.systemankiet.entity.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class QuestionDto {

    // Nazwy pol odpowiadaja kluczom JSON z frontendu: "text", "type", "isRequired", "options"
    @NotBlank(message = "Tresc pytania nie moze byc pusta")
    private String text;

    @NotBlank(message = "Typ pytania jest wymagany")
    private String type;

    // @JsonProperty zapewnia ze JSON ma klucz "isRequired" a nie "required"
    @JsonProperty("isRequired")
    private boolean required;

    private List<String> options = new ArrayList<>();

    // Pole wyjsciowe (id po zapisie)
    private Long id;

    public static QuestionDto fromEntity(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getQuestionText());
        dto.setType(question.getQuestionType());
        dto.setRequired(question.isRequired());
        dto.setOptions(
            question.getOptions().stream()
                .map(opt -> opt.getOptionText())
                .collect(Collectors.toList())
        );
        return dto;
    }
}
