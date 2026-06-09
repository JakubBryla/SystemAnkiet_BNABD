package com.systemankiet.service;

import com.systemankiet.dto.CreateSurveyRequest;
import com.systemankiet.dto.QuestionDto;
import com.systemankiet.dto.SurveyDetailDto;
import com.systemankiet.dto.SurveyDto;
import com.systemankiet.dto.UpdateSurveyStatusRequest;
import com.systemankiet.entity.AnswerOption;
import com.systemankiet.entity.Question;
import com.systemankiet.entity.Survey;
import com.systemankiet.entity.User;
import com.systemankiet.enums.SurveyStatus;
import com.systemankiet.enums.SurveyType;
import com.systemankiet.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    @Transactional(readOnly = true)
    public List<SurveyDto> getUserSurveys(User user) {
        return surveyRepository.findByCreatedByOrderByCreatedAtDesc(user)
                .stream()
                .map(SurveyDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Ankiety wewnetrzne przypisane do zalogowanego uzytkownika (z jego organizacji)
    @Transactional(readOnly = true)
    public List<SurveyDto> getAssignedSurveys(User user) {
        if (user.getOrganization() == null) {
            return new ArrayList<>();
        }
        return surveyRepository.findAssignedSurveys(
                SurveyType.INTERNAL,
                SurveyStatus.ACTIVE,
                user.getOrganization(),
                user
            )
            .stream()
            .map(SurveyDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public SurveyDto createSurvey(CreateSurveyRequest request, User user) {
        Survey survey = new Survey();
        survey.setTitle(request.getTitle().trim());
        survey.setDescription(request.getDescription());
        survey.setCreatedBy(user);
        survey.setType(parseSurveyType(request.getType()));
        survey.setQuestions(buildQuestions(request.getQuestions(), survey));

        return SurveyDto.fromEntity(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyDto updateSurvey(Long id, CreateSurveyRequest request, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));

        survey.setTitle(request.getTitle().trim());
        survey.setDescription(request.getDescription());
        if (request.getType() != null) {
            survey.setType(parseSurveyType(request.getType()));
        }

        survey.getQuestions().clear();
        survey.getQuestions().addAll(buildQuestions(request.getQuestions(), survey));

        return SurveyDto.fromEntity(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyDto updateSurveyStatus(Long id, UpdateSurveyStatusRequest request, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));

        SurveyStatus newStatus = SurveyStatus.valueOf(request.getStatus().toUpperCase());
        survey.setStatus(newStatus);

        return SurveyDto.fromEntity(surveyRepository.save(survey));
    }

    @Transactional(readOnly = true)
    public SurveyDetailDto getPublicSurvey(Long id) {
        Survey survey = surveyRepository.findByIdAndStatus(id, SurveyStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona"));
        return SurveyDetailDto.fromEntity(survey);
    }

    @Transactional
    public void deleteSurvey(Long id, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));
        surveyRepository.delete(survey);
    }

    // --- Metody pomocnicze ---

    private SurveyType parseSurveyType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) return SurveyType.EXTERNAL;
        try {
            return SurveyType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SurveyType.EXTERNAL;
        }
    }

    private List<Question> buildQuestions(List<QuestionDto> questionDtos, Survey survey) {
        if (questionDtos == null) return new ArrayList<>();

        List<Question> result = new ArrayList<>();
        for (QuestionDto qDto : questionDtos) {
            if (qDto == null) continue;

            Question question = new Question();
            question.setQuestionText(qDto.getText().trim());
            question.setQuestionType(qDto.getType().trim());
            question.setRequired(Boolean.TRUE.equals(qDto.getIsRequired()));
            question.setControlQuestion(Boolean.TRUE.equals(qDto.getIsControlQuestion()));
            question.setExpectedValue(qDto.getExpectedValue());
            question.setFailStatus(qDto.getFailStatus());
            question.setSurvey(survey);
            question.setOptions(new ArrayList<>());

            if (qDto.getOptions() != null) {
                for (String optText : qDto.getOptions()) {
                    if (optText == null || optText.isBlank()) continue;
                    AnswerOption option = new AnswerOption();
                    option.setOptionText(optText.trim());
                    option.setQuestion(question);
                    question.getOptions().add(option);
                }
            }

            result.add(question);
        }
        return result;
    }
}
