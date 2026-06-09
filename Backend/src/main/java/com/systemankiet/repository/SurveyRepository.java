package com.systemankiet.repository;

import com.systemankiet.entity.Survey;
import com.systemankiet.entity.User;
import com.systemankiet.enums.SurveyStatus;
import com.systemankiet.enums.SurveyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByCreatedByOrderByCreatedAtDesc(User user);

    Optional<Survey> findByIdAndCreatedBy(Long id, User user);

    // Ankiety przypisane do uzytkownika: wewnetrzne, aktywne, z tej samej domeny,
    // nie stworzone przez niego i jeszcze przez niego nie wypełnione
    @Query("SELECT s FROM Survey s WHERE s.type = :type AND s.status = :status " +
           "AND s.createdBy.domain = :domain AND s.createdBy <> :user " +
           "AND NOT EXISTS (SELECT r FROM SurveyResponse r WHERE r.survey = s AND r.respondent = :user) " +
           "ORDER BY s.createdAt DESC")
    List<Survey> findAssignedSurveys(
        @Param("type") SurveyType type,
        @Param("status") SurveyStatus status,
        @Param("domain") String domain,
        @Param("user") User user
    );
}
