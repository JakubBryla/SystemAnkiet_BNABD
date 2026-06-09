package com.systemankiet.service;

import com.systemankiet.dto.ResponseDto;
import com.systemankiet.dto.SubmitResponseRequest;
import com.systemankiet.entity.*;
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

@Service
@RequiredArgsConstructor
public class ResponseService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;

    @Transactional
    public ResponseDto submitResponse(Long surveyId, SubmitResponseRequest request, User currentUser) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona"));

        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new IllegalArgumentException("Ankieta nie jest aktywna");
        }

        // Ankieta wewnetrzna - sprawdz czy uzytkownik nalezy do tej samej organizacji
        if (survey.getType() == SurveyType.INTERNAL) {
            if (currentUser == null) {
                throw new IllegalArgumentException("Ankieta wewnetrzna wymaga zalogowania");
            }
            Organization surveyOrg = survey.getCreatedBy().getOrganization();
            Organization userOrg = currentUser.getOrganization();
            if (surveyOrg == null || userOrg == null || !surveyOrg.getId().equals(userOrg.getId())) {
                throw new IllegalArgumentException("Brak dostepu do ankiety wewnetrznej swojej organizacji");
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
