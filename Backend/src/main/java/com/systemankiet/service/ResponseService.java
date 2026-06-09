package com.systemankiet.service;

import com.systemankiet.dto.AnswerDetailDto;
import com.systemankiet.dto.ResponseDetailDto;
import com.systemankiet.dto.ResponseDto;
import com.systemankiet.dto.SubmitResponseRequest;
import com.systemankiet.entity.Question;
import com.systemankiet.entity.ResponseAnswer;
import com.systemankiet.entity.Survey;
import com.systemankiet.entity.SurveyResponse;
import com.systemankiet.entity.User;
import com.systemankiet.enums.SurveyStatus;
import com.systemankiet.enums.SurveyType;
import com.systemankiet.repository.SurveyRepository;
import com.systemankiet.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponseService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;

    @Transactional(readOnly = true)
    public List<ResponseDetailDto> getResponses(Long surveyId, User currentUser) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona"));

        // Tylko twórca ankiety może przeglądać odpowiedzi
        if (!survey.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Brak uprawnien do przeglądania odpowiedzi tej ankiety");
        }

        return responseRepository.findBySurveyOrderBySubmittedAtDesc(survey)
                .stream()
                .map(ResponseDetailDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseDto submitResponse(Long surveyId, SubmitResponseRequest request, User currentUser) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona"));

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalArgumentException("Ankieta nie jest aktywna");
        }

        // Ankieta wewnetrzna - sprawdz czy uzytkownik ma tę samą domenę emaila
        if (survey.getType() == SurveyType.INTERNAL) {
            if (currentUser == null) {
                throw new IllegalArgumentException("Ankieta wewnetrzna wymaga zalogowania");
            }
            String surveyDomain = survey.getCreatedBy().getDomain();
            String userDomain = currentUser.getDomain();
            if (surveyDomain == null || !surveyDomain.equalsIgnoreCase(userDomain)) {
                throw new IllegalArgumentException("Brak dostepu – ankieta dostepna tylko dla uzytkownikow z domeny: " + surveyDomain);
            }
        }

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);

        boolean flagged = false;
        String flagReason = null;
        String flagStatus = null;

        Map<Long, String> answerMap = request.getAnswers() != null ? request.getAnswers() : Map.of();
        List<ResponseAnswer> answers = new ArrayList<>();

        for (Question question : survey.getQuestions()) {
            String answerValue = answerMap.get(question.getId());

            // Zapisz odpowiedz
            ResponseAnswer answer = new ResponseAnswer();
            answer.setResponse(response);
            answer.setQuestion(question);
            answer.setAnswerValue(answerValue);
            answers.add(answer);

            // Walidacja pytania kontrolnego
            if (question.isControlQuestion()
                    && question.getExpectedValue() != null
                    && answerValue != null) {

                boolean correct = question.getExpectedValue().trim()
                        .equalsIgnoreCase(answerValue.trim());

                if (!correct) {
                    String status = question.getFailStatus();

                    // BLOCK - nie pozwol wyslac formularza
                    if ("BLOCK".equalsIgnoreCase(status)) {
                        throw new IllegalArgumentException(
                            "Nieprawidlowa odpowiedz na pytanie kontrolne: " + question.getQuestionText()
                        );
                    }

                    // WARNING / UNRELIABLE - oznacz odpowiedz (UNRELIABLE ma wyzszy priorytet)
                    if (!flagged || "UNRELIABLE".equalsIgnoreCase(status)) {
                        flagged = true;
                        flagStatus = status;
                        flagReason = "Pytanie kontrolne: " + question.getQuestionText();
                    }
                }
            }
        }

        response.setFlagged(flagged);
        response.setFlagReason(flagReason);
        response.setFlagStatus(flagStatus);
        response.setAnswers(answers);

        return ResponseDto.fromEntity(responseRepository.save(response));
    }
}
