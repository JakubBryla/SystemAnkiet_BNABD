package com.systemankiet.repository;

import com.systemankiet.entity.Survey;
import com.systemankiet.entity.SurveyResponse;
import com.systemankiet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findBySurveyOrderBySubmittedAtDesc(Survey survey);

    boolean existsBySurvey(Survey survey);

    // Sprawdza czy zalogowany użytkownik już wypełnił daną ankietę (blokada duplikatów dla INTERNAL)
    boolean existsBySurveyAndRespondent(Survey survey, User respondent);

    long countBySurvey(Survey survey);

    void deleteAllBySurvey(Survey survey);
}
