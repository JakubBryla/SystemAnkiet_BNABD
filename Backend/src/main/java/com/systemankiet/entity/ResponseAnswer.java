package com.systemankiet.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Encja odpowiedzi na jedno pytanie — tabela "response_answers".
 * Powiązana z konkretnym wypełnieniem (SurveyResponse) i pytaniem (Question).
 * answerValue przechowuje tekst odpowiedzi; dla multiple-choice wartości są oddzielone przecinkami.
 */
@Entity
@Table(name = "response_answers")
@Getter
@Setter
@NoArgsConstructor
public class ResponseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_value", columnDefinition = "NVARCHAR(MAX)")
    private String answerValue;
}
