package com.systemankiet.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Encja opcji odpowiedzi — tabela "answer_options".
 * Reprezentuje jedną możliwą opcję wyboru dla pytania typu single-choice lub multiple-choice.
 * Tworzona przez ankietera przy budowaniu ankiety; kolejność opcji gwarantuje @OrderColumn(option_order).
 */
@Entity
@Table(name = "answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String optionText;
}
