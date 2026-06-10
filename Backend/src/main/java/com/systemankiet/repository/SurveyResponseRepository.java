package com.systemankiet.repository;

import com.systemankiet.entity.Survey;
import com.systemankiet.entity.SurveyResponse;
import com.systemankiet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium wypełnień ankiet. Zawiera metody do sprawdzania duplikatów per-okres-aktywności
 * oraz ładowania odpowiedzi z JOIN FETCH (eliminacja problemu N+1).
 */
@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    // N+1 fix: JOIN FETCH laduje odpowiedzi razem z ich odpowiedziami i pytaniami w jednym zapytaniu
    @Query("SELECT DISTINCT r FROM SurveyResponse r " +
           "LEFT JOIN FETCH r.answers a " +
           "LEFT JOIN FETCH a.question " +
           "WHERE r.survey = :survey " +
           "ORDER BY r.submittedAt DESC")
    List<SurveyResponse> findBySurveyOrderBySubmittedAtDesc(@Param("survey") Survey survey);

    boolean existsBySurvey(Survey survey);

    // Sprawdza czy użytkownik wypełnił ankietę PO danej dacie (bieżący okres aktywności).
    // Używane zamiast existsBySurveyAndRespondent — pozwala na ponowne wypełnienie
    // po zamknięciu i ponownym otwarciu ankiety (nowy okres = nowa lastActivatedAt).
    boolean existsBySurveyAndRespondentAndSubmittedAtAfter(Survey survey, User respondent, LocalDateTime submittedAt);

    // Fallback dla ankiet bez lastActivatedAt (starsze rekordy sprzed tej funkcji)
    boolean existsBySurveyAndRespondent(Survey survey, User respondent);

    long countBySurvey(Survey survey);

    void deleteAllBySurvey(Survey survey);
}
