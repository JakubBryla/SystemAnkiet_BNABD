package com.systemankiet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_responses")
@Getter
@Setter
@NoArgsConstructor
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // Zalogowany użytkownik który wypełnił ankietę (null dla ankiet EXTERNAL — brak konta)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id")
    private User respondent;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Czy odpowiedz zostala oznaczona przez pytanie kontrolne
    @Column(name = "is_flagged", nullable = false)
    private boolean flagged;

    // Tresc komunikatu z pytania kontrolnego (np. "Pytanie kontrolne: Wiek")
    @Column(name = "flag_reason")
    private String flagReason;

    // Wartosc severity: WARNING lub UNRELIABLE
    @Column(name = "flag_status")
    private String flagStatus;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResponseAnswer> answers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
