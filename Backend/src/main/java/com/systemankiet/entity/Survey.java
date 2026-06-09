package com.systemankiet.entity;

import com.systemankiet.enums.SurveyStatus;
import com.systemankiet.enums.SurveyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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
