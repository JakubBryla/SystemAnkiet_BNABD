package com.systemankiet.service;

import com.systemankiet.dto.CreateSurveyRequest;
import com.systemankiet.dto.SurveyDto;
import com.systemankiet.entity.Survey;
import com.systemankiet.entity.User;
import com.systemankiet.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public List<SurveyDto> getUserSurveys(User user) {
        return surveyRepository.findByCreatedByOrderByCreatedAtDesc(user)
                .stream()
                .map(SurveyDto::fromEntity)
                .collect(Collectors.toList());
    }

    public SurveyDto createSurvey(CreateSurveyRequest request, User user) {
        Survey survey = Survey.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        return SurveyDto.fromEntity(surveyRepository.save(survey));
    }

    public void deleteSurvey(Long id, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));

        surveyRepository.delete(survey);
    }
}
