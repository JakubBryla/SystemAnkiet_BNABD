package com.systemankiet.repository;

import com.systemankiet.entity.Survey;
import com.systemankiet.entity.User;
import com.systemankiet.enums.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByCreatedByOrderByCreatedAtDesc(User user);

    Optional<Survey> findByIdAndCreatedBy(Long id, User user);

    Optional<Survey> findByIdAndStatus(Long id, SurveyStatus status);
}
