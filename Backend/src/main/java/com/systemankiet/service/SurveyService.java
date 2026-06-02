package com.systemankiet.service;

import com.systemankiet.dto.CreateSurveyRequest;
import com.systemankiet.dto.QuestionDto;
import com.systemankiet.dto.SurveyDto;
import com.systemankiet.entity.AnswerOption;
import com.systemankiet.entity.Question;
import com.systemankiet.entity.Survey;
import com.systemankiet.entity.User;
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

        Survey saved = surveyRepository.save(survey);
        return SurveyDto.fromEntity(saved);
    }

    @Transactional
    public void deleteSurvey(Long id, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));
        surveyRepository.delete(survey);
    }
}
