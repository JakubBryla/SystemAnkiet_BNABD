package com.systemankiet.repository;

import com.systemankiet.entity.Organization;
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

    // Ankiety przypisane do uzytkownika: wewnetrzne, aktywne, z tej samej organizacji, nie stworzone przez niego
    @Query("SELECT s FROM Survey s WHERE s.type = :type AND s.status = :status " +
           "AND s.createdBy.organization = :org AND s.createdBy <> :excludeUser " +
           "ORDER BY s.createdAt DESC")
    List<Survey> findAssignedSurveys(
        @Param("type") SurveyType type,
        @Param("status") SurveyStatus status,
        @Param("org") Organization org,
        @Param("excludeUser") User excludeUser
    );
}
