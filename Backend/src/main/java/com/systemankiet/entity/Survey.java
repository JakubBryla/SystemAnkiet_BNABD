package com.systemankiet.entity;

import com.systemankiet.enums.SurveyStatus;
import com.systemankiet.enums.SurveyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encja ankiety — tabela "surveys".
 * Ankieta może być INTERNAL (tylko pracownicy tej samej domeny) lub EXTERNAL (publiczna przez link).
 * lastActivatedAt jest aktualizowane przy każdym przejściu na status ACTIVE — używane do wyznaczania
 * bieżącego okresu aktywności i umożliwia ponowne wypełnienie po zamknięciu i wznowieniu ankiety.
 */
@Entity
@Table(name = "surveys")
@Getter
@Setter
@NoArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;

    // EXTERNAL - link publiczny, każdy może wypełnić bez logowania
    // INTERNAL - tylko zalogowani użytkownicy tej samej organizacji
    // columnDefinition z DEFAULT pozwala Hibernate dodać kolumnę do niepustej tabeli (SQL Server)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "NVARCHAR(255) DEFAULT 'EXTERNAL'")
    private SurveyType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Aktualizowana przy każdym przejściu w status ACTIVE.
    // Służy do określenia "bieżącego okresu aktywności" — duplikaty sprawdzane są
    // tylko wśród odpowiedzi złożonych po tej dacie, co pozwala na ponowne
    // wypełnienie ankiety po jej zamknięciu i ponownym otwarciu.
    @Column(name = "last_activated_at")
    private LocalDateTime lastActivatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "question_order")
    private List<Question> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SurveyStatus.DRAFT;
        }
        if (type == null) {
            type = SurveyType.EXTERNAL;
        }
    }
}
