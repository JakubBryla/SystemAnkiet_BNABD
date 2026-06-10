package com.systemankiet.repository;

import com.systemankiet.entity.Survey;
import com.systemankiet.entity.SurveyResponse;
import com.systemankiet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    // Sprawdza czy zalogowany użytkownik już wypełnił daną ankietę (blokada duplikatów dla INTERNAL)
    boolean existsBySurveyAndRespondent(Survey survey, User respondent);

    long countBySurvey(Survey survey);

    void deleteAllBySurvey(Survey survey);
}
