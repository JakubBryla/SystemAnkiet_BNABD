package com.systemankiet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Encja pytania — tabela "questions".
 * Typy pytań: short-answer (tekst), single-choice (jedna opcja), multiple-choice (wiele opcji).
 * Pytanie kontrolne (isControlQuestion) służy do weryfikacji wiarygodności respondenta —
 * jeśli odpowiedź nie zgadza się z expectedValue, całe wypełnienie jest oznaczane jako UNRELIABLE.
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    @Column(name = "question_type", nullable = false)
    private String questionType;

    @Column(name = "is_required", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean required;

    // Czy to pytanie kontrolne (służy do weryfikacji wiarygodności odpowiedzi)
    @Column(name = "is_control_question", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private boolean controlQuestion;

    // Oczekiwana poprawna odpowiedź (wypełniana gdy is_control_question = true)
    @Column(name = "expected_value")
    private String expectedValue;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "option_order")
    private List<AnswerOption> options = new ArrayList<>();
}
