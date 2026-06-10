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
import com.systemankiet.exception.DuplicateSubmissionException;
import com.systemankiet.repository.SurveyRepository;
import com.systemankiet.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Serwis odpowiedzi — zapis wypełnień ankiet i pobieranie wyników dla ankietera.
 * Przy zapisie: weryfikuje status ankiety, domenę użytkownika (INTERNAL), duplikaty per-okres-aktywności
 * oraz automatycznie flaguje odpowiedzi z błędnymi odpowiedziami na pytania kontrolne.
 */
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
            // Sprawdź czy użytkownik już wypełnił ankietę W BIEŻĄCYM OKRESIE AKTYWNOŚCI.
            // Jeśli lastActivatedAt jest ustawione — sprawdzamy tylko odpowiedzi po tej dacie,
            // co pozwala na ponowne wypełnienie po zamknięciu i ponownym otwarciu ankiety.
            // Fallback na starsze rekordy (bez lastActivatedAt) — sprawdzamy wszystkie.
            LocalDateTime since = survey.getLastActivatedAt();
            boolean alreadySubmitted = (since != null)
                    ? responseRepository.existsBySurveyAndRespondentAndSubmittedAtAfter(survey, currentUser, since)
                    : responseRepository.existsBySurveyAndRespondent(survey, currentUser);
            if (alreadySubmitted) {
                throw new DuplicateSubmissionException("Ta ankieta została już przez Ciebie wypełniona");
            }
        }

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setRespondent(currentUser); // null dla EXTERNAL — brak konta

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

            // Walidacja pytania kontrolnego — niepoprawna odpowiedź oznacza wypełnienie jako niewiarygodne
            if (question.isControlQuestion()
                    && question.getExpectedValue() != null
                    && answerValue != null) {

                boolean correct = question.getExpectedValue().trim()
                        .equalsIgnoreCase(answerValue.trim());

                if (!correct && !flagged) {
                    flagged = true;
                    flagStatus = "UNRELIABLE";
                    flagReason = "Pytanie kontrolne: " + question.getQuestionText();
                }
            }
        }

        response.setFlagged(flagged);
        response.setFlagReason(flagReason);
        response.setFlagStatus(flagStatus);
        response.setAnswers(answers);

        try {
            return ResponseDto.fromEntity(responseRepository.save(response));
        } catch (DataIntegrityViolationException e) {
            // Unikalny indeks UQ_survey_responses_survey_respondent został usunięty
            // (DatabaseMigrationRunner go dropuje) — ten catch obsługuje inne błędy bazy
            // np. naruszenie FK lub NOT NULL, które powinny dostać własny komunikat.
            throw e;
        }
    }
}
