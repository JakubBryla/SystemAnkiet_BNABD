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

    @Transactional
    public SurveyDto createSurvey(CreateSurveyRequest request, User user) {
        Survey survey = new Survey();
        survey.setTitle(request.getTitle().trim());
        survey.setDescription(request.getDescription());
        survey.setCreatedBy(user);
        survey.setQuestions(new ArrayList<>());

        if (request.getQuestions() != null) {
            for (QuestionDto qDto : request.getQuestions()) {
                if (qDto == null) continue;

                Question question = new Question();
                question.setQuestionText(qDto.getText().trim());
                question.setQuestionType(qDto.getType().trim());
                question.setRequired(qDto.isRequired());
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

                survey.getQuestions().add(question);
            }
        }

        return SurveyDto.fromEntity(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyDto updateSurvey(Long id, CreateSurveyRequest request, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));

        survey.setTitle(request.getTitle().trim());
        survey.setDescription(request.getDescription());

        // Usuniecie starych pytan (orphanRemoval = true usuwa je z bazy)
        survey.getQuestions().clear();

        if (request.getQuestions() != null) {
            for (QuestionDto qDto : request.getQuestions()) {
                if (qDto == null) continue;

                Question question = new Question();
                question.setQuestionText(qDto.getText().trim());
                question.setQuestionType(qDto.getType().trim());
                question.setRequired(qDto.isRequired());
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

                survey.getQuestions().add(question);
            }
        }

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
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona"));
        return SurveyDetailDto.fromEntity(survey);
    }

    @Transactional
    public void deleteSurvey(Long id, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));
        surveyRepository.delete(survey);
    }
}
