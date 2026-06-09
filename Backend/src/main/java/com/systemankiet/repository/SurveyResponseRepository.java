package com.systemankiet.repository;

import com.systemankiet.entity.Survey;
import com.systemankiet.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findBySurveyOrderBySubmittedAtDesc(Survey survey);

    boolean existsBySurvey(Survey survey);
}
