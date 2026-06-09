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
import com.systemankiet.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SurveyResponseRepository responseRepository;

    // Paginacja po stronie backendu z filtrami przekazanymi jako parametry zapytania
    @Transactional(readOnly = true)
    public Page<SurveyDto> getUserSurveys(User user, String search, String statusStr, String typeStr, Pageable pageable) {
        SurveyStatus status = parseStatusFilter(statusStr);
        SurveyType type = parseTypeFilter(typeStr);
        String searchTerm = (search == null) ? "" : search;
        return surveyRepository.findByCreatedByWithFilters(user, searchTerm, status, type, pageable)
                .map(SurveyDto::fromEntity);
    }

    // Ankiety wewnetrzne przypisane do zalogowanego uzytkownika (z tej samej domeny emaila)
    @Transactional(readOnly = true)
    public List<SurveyDto> getAssignedSurveys(User user) {
        if (user.getDomain() == null || user.getDomain().isBlank()) {
            return new ArrayList<>();
        }
        return surveyRepository.findAssignedSurveys(
                SurveyType.INTERNAL,
                SurveyStatus.ACTIVE,
                user.getDomain(),
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

        // Pytania można modyfikować tylko jeśli nie ma jeszcze żadnych odpowiedzi.
        // Usunięcie pytań z istniejącymi odpowiedziami naruszyłoby FK response_answers.question_id.
        if (!responseRepository.existsBySurvey(survey)) {
            survey.getQuestions().clear();
            survey.getQuestions().addAll(buildQuestions(request.getQuestions(), survey));
        }
        // Jeśli odpowiedzi istnieją — metadata (tytuł/opis/typ) zostaje zaktualizowana,
        // a pytania pozostają bez zmian (zablokowane ze względu na spójność danych).

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
        // N+1 fix: findByIdWithDetails uzywa JOIN FETCH — pytania i opcje ladowane w jednym zapytaniu
        Survey survey = surveyRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona"));
        SurveyDetailDto dto = SurveyDetailDto.fromEntity(survey);
        long count = responseRepository.countBySurvey(survey);
        dto.setHasResponses(count > 0);
        dto.setResponseCount(count);
        return dto;
    }

    @Transactional
    public void deleteSurvey(Long id, User user) {
        Survey survey = surveyRepository.findByIdAndCreatedBy(id, user)
                .orElseThrow(() -> new NoSuchElementException("Ankieta nie znaleziona lub brak uprawnien"));

        // Najpierw usuń odpowiedzi (kaskadowo usuwa response_answers przez CascadeType.ALL).
        // Bez tego SQL Server blokuje usunięcie questions przez FK response_answers.question_id.
        responseRepository.deleteAllBySurvey(survey);

        surveyRepository.delete(survey);
    }

    // --- Metody pomocnicze ---

    // Parsuje filtr statusu — "all" lub null/pusty zwraca null (brak filtra)
    private SurveyStatus parseStatusFilter(String s) {
        if (s == null || s.isBlank() || "all".equalsIgnoreCase(s)) return null;
        try { return SurveyStatus.valueOf(s.toUpperCase()); } catch (Exception e) { return null; }
    }

    // Parsuje filtr typu — "all" lub null/pusty zwraca null (brak filtra)
    private SurveyType parseTypeFilter(String s) {
        if (s == null || s.isBlank() || "all".equalsIgnoreCase(s)) return null;
        try { return SurveyType.valueOf(s.toUpperCase()); } catch (Exception e) { return null; }
    }

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
