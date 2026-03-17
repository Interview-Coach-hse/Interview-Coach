package interview.coach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.coach.config.AssessmentClientProperties;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.MessageType;
import interview.coach.domain.DomainEnums.ReportItemType;
import interview.coach.domain.DomainEnums.SenderType;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.domain.entity.ProfileQuestion;
import interview.coach.domain.entity.ProfileTag;
import interview.coach.domain.entity.SessionMessage;
import interview.coach.integration.assessment.AssessmentDtos.Metadata;
import interview.coach.integration.assessment.AssessmentDtos.QuestionItem;
import interview.coach.integration.assessment.AssessmentDtos.QuestionReport;
import interview.coach.integration.assessment.AssessmentDtos.QuestionsResponse;
import interview.coach.integration.assessment.AssessmentDtos.Report;
import interview.coach.integration.assessment.AssessmentDtos.ReportItemRequest;
import interview.coach.integration.assessment.AssessmentDtos.ReportRequest;
import interview.coach.integration.assessment.AssessmentDtos.ReportResponse;
import interview.coach.integration.assessment.AssessmentDtos.Scenario;
import interview.coach.repository.ProfileQuestionRepository;
import interview.coach.repository.ProfileTagRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AssessmentAiService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentAiService.class);
    private static final DateTimeFormatter OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final RestClient assessmentRestClient;
    private final AssessmentClientProperties properties;
    private final ProfileQuestionRepository profileQuestionRepository;
    private final ProfileTagRepository profileTagRepository;
    private final ObjectMapper objectMapper;

    public AssessmentAiService(
            RestClient assessmentRestClient,
            AssessmentClientProperties properties,
            ProfileQuestionRepository profileQuestionRepository,
            ProfileTagRepository profileTagRepository,
            ObjectMapper objectMapper
    ) {
        this.assessmentRestClient = assessmentRestClient;
        this.properties = properties;
        this.profileQuestionRepository = profileQuestionRepository;
        this.profileTagRepository = profileTagRepository;
        this.objectMapper = objectMapper;
    }

    public NextPromptResult getNextPrompt(InterviewSession session, int zeroBasedQuestionIndex) {
        InterviewProfile profile = session.getProfile();
        Map<String, Object> requestView = Map.of(
                "specialization", toSpecialization(profile.getDirection()),
                "grade", toGrade(profile.getLevel()),
                "limit", Math.max(properties.questionLimit(), zeroBasedQuestionIndex + 1)
        );

        if (properties.enabled()) {
            try {
                QuestionsResponse response = assessmentRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/assessment/v1/questions")
                                .queryParam("specialization", toSpecialization(profile.getDirection()))
                                .queryParam("grade", toGrade(profile.getLevel()))
                                .queryParam("limit", Math.max(properties.questionLimit(), zeroBasedQuestionIndex + 1))
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(QuestionsResponse.class);

                List<QuestionItem> items = response == null || response.items() == null ? List.of() : response.items();
                if (zeroBasedQuestionIndex < items.size()) {
                    QuestionItem selected = items.get(zeroBasedQuestionIndex);
                    return new NextPromptResult(
                            SenderType.INTERVIEWER,
                            MessageType.QUESTION,
                            selected.questionText(),
                            true,
                            writeJson(requestView),
                            writeJson(response),
                            true,
                            null
                    );
                }
                log.warn("External assessment returned only {} question(s) for profile {}, fallback will be used",
                        items.size(), profile.getId());
            } catch (Exception exception) {
                log.error("Failed to fetch next question from external assessment service for session {}", session.getId(), exception);
                return fallbackPrompt(profile, zeroBasedQuestionIndex, requestView, exception.getMessage());
            }
        }

        return fallbackPrompt(profile, zeroBasedQuestionIndex, requestView, properties.enabled()
                ? "External service returned no question for requested index"
                : null);
    }

    public AssessmentReportResult generateReport(InterviewSession session, List<SessionMessage> messages) {
        ReportRequest request = buildReportRequest(session, messages);
        String requestPayload = writeJson(request);

        if (properties.enabled()) {
            try {
                ReportResponse response = assessmentRestClient.post()
                        .uri("/assessment/v1/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(ReportResponse.class);

                if (response == null || response.report() == null) {
                    throw new IllegalStateException("External assessment returned empty report");
                }
                if (!"sync".equalsIgnoreCase(properties.mode())) {
                    throw new IllegalStateException("Async report mode is not supported by the current backend flow");
                }
                return toExternalReportResult(requestPayload, response);
            } catch (Exception exception) {
                log.error("Failed to generate report via external assessment service for session {}", session.getId(), exception);
                AssessmentReportResult fallback = buildLocalFallback(messages, requestPayload);
                return new AssessmentReportResult(
                        fallback.summary(),
                        fallback.overallScore(),
                        fallback.items(),
                        requestPayload,
                        fallback.responsePayload(),
                        false,
                        exception.getMessage()
                );
            }
        }

        return buildLocalFallback(messages, requestPayload);
    }

    private NextPromptResult fallbackPrompt(
            InterviewProfile profile,
            int zeroBasedQuestionIndex,
            Map<String, Object> requestView,
            String errorMessage
    ) {
        List<ProfileQuestion> profileQuestions = profileQuestionRepository.findByProfileIdOrderByOrderIndexAsc(profile.getId());
        if (zeroBasedQuestionIndex < profileQuestions.size()) {
            ProfileQuestion profileQuestion = profileQuestions.get(zeroBasedQuestionIndex);
            return new NextPromptResult(
                    SenderType.INTERVIEWER,
                    MessageType.QUESTION,
                    profileQuestion.getQuestion().getText(),
                    true,
                    writeJson(requestView),
                    writeJson(Map.of(
                            "source", "internal-profile-questions",
                            "questionId", profileQuestion.getQuestion().getId(),
                            "orderIndex", profileQuestion.getOrderIndex(),
                            "required", profileQuestion.isRequired()
                    )),
                    !properties.enabled(),
                    errorMessage
            );
        }

        return new NextPromptResult(
                SenderType.SYSTEM,
                MessageType.INFO,
                "Банк вопросов для этого сценария закончился. Можно завершить сессию и запросить отчёт.",
                false,
                writeJson(requestView),
                writeJson(Map.of("source", "internal-profile-questions", "message", "no-more-questions")),
                !properties.enabled(),
                errorMessage
        );
    }

    private ReportRequest buildReportRequest(InterviewSession session, List<SessionMessage> messages) {
        InterviewProfile profile = session.getProfile();
        List<String> topics = profileTagRepository.findByProfileId(profile.getId()).stream()
                .map(ProfileTag::getTag)
                .map(tag -> normalizeTopicCode(tag.getName()))
                .distinct()
                .toList();

        List<ReportItemRequest> items = pairMessages(messages).stream()
                .map(pair -> new ReportItemRequest(
                        "item-" + pair.index(),
                        "session-" + session.getId() + "-q-" + pair.index(),
                        pair.questionText(),
                        pair.answerText(),
                        pair.askedAt().atOffset(ZoneOffset.UTC).format(OFFSET_DATE_TIME),
                        topics
                ))
                .toList();

        return new ReportRequest(
                UUID.randomUUID().toString(),
                session.getId().toString(),
                properties.clientId(),
                properties.mode(),
                new Scenario(
                        profile.getId().toString(),
                        toSpecialization(profile.getDirection()),
                        toGrade(profile.getLevel()),
                        topics,
                        properties.reportLanguage()
                ),
                items,
                new Metadata(properties.clientId(), properties.subscriptionPlan())
        );
    }

    private AssessmentReportResult toExternalReportResult(String requestPayload, ReportResponse response) {
        Report report = response.report();
        List<AssessmentReportItemDraft> items = new ArrayList<>();
        int sortOrder = 0;

        List<String> strengths = report.questions() == null ? List.of() : report.questions().stream()
                .flatMap(question -> safeList(question.strengths()).stream())
                .distinct()
                .limit(3)
                .toList();
        for (String strength : strengths) {
            items.add(new AssessmentReportItemDraft(ReportItemType.STRENGTH, "Сильная сторона", strength, null, sortOrder++));
        }

        List<String> weaknesses = new ArrayList<>();
        if (report.questions() != null) {
            for (QuestionReport question : report.questions()) {
                weaknesses.addAll(safeList(question.issues()));
                weaknesses.addAll(safeList(question.missingKeypoints()));
            }
        }
        if (report.topics() != null) {
            report.topics().forEach(topic -> weaknesses.addAll(safeList(topic.gaps())));
        }
        for (String weakness : weaknesses.stream().distinct().limit(3).toList()) {
            items.add(new AssessmentReportItemDraft(ReportItemType.WEAKNESS, "Зона роста", weakness, null, sortOrder++));
        }

        List<String> recommendations = new ArrayList<>(safeList(report.recommendations()));
        if (report.questions() != null) {
            report.questions().forEach(question -> recommendations.addAll(safeList(question.recommendations())));
        }
        for (String recommendation : recommendations.stream().distinct().limit(3).toList()) {
            items.add(new AssessmentReportItemDraft(ReportItemType.RECOMMENDATION, "Рекомендация", recommendation, null, sortOrder++));
        }

        if (report.overallScore() != null) {
            items.add(new AssessmentReportItemDraft(
                    ReportItemType.CATEGORY_SCORE,
                    "Overall score",
                    "Итоговая оценка по внешнему assessment-сервису.",
                    report.overallScore().setScale(2, RoundingMode.HALF_UP),
                    sortOrder++
            ));
        }
        if (report.criterionScores() != null) {
            for (Map.Entry<String, BigDecimal> entry : report.criterionScores().entrySet()) {
                items.add(new AssessmentReportItemDraft(
                        ReportItemType.CATEGORY_SCORE,
                        "Criterion: " + entry.getKey(),
                        "Оценка по критерию " + entry.getKey() + ".",
                        entry.getValue() == null ? null : entry.getValue().setScale(2, RoundingMode.HALF_UP),
                        sortOrder++
                ));
            }
        }

        return new AssessmentReportResult(
                report.summary(),
                report.overallScore() == null ? null : report.overallScore().setScale(2, RoundingMode.HALF_UP),
                items,
                requestPayload,
                writeJson(response),
                true,
                null
        );
    }

    private AssessmentReportResult buildLocalFallback(List<SessionMessage> messages, String requestPayload) {
        long answers = messages.stream().filter(message -> message.getSenderType() == SenderType.USER).count();
        int totalAnswerLength = messages.stream()
                .filter(message -> message.getSenderType() == SenderType.USER)
                .mapToInt(message -> message.getContent().length())
                .sum();
        BigDecimal overallScore = BigDecimal.valueOf(Math.min(100, 40 + (answers * 10) + Math.min(totalAnswerLength / 40, 30)))
                .setScale(2, RoundingMode.HALF_UP);

        String summary = answers == 0
                ? "Сессия завершена без ответов пользователя. Требуется пройти интервью заново."
                : "Сессия завершена. Внешний AI недоступен, поэтому применена локальная эвристическая оценка по сохранённым ответам.";

        List<AssessmentReportItemDraft> items = List.of(
                new AssessmentReportItemDraft(ReportItemType.STRENGTH, "Сильная сторона", "История диалога и ответы пользователя были успешно сохранены.", overallScore, 0),
                new AssessmentReportItemDraft(ReportItemType.WEAKNESS, "Зона роста", answers < 3
                        ? "Ответов пока мало для глубокой оценки. Стоит пройти более длинную сессию."
                        : "Для более точной диагностики нужен внешний AI assessment-сервис.", answers < 3 ? BigDecimal.valueOf(35) : BigDecimal.valueOf(55), 1),
                new AssessmentReportItemDraft(ReportItemType.RECOMMENDATION, "Рекомендация", "Повторите сценарий и дайте более развёрнутые ответы для улучшения итоговой оценки.", null, 2),
                new AssessmentReportItemDraft(ReportItemType.CATEGORY_SCORE, "Итоговый балл", "Локальная оценка без внешнего AI.", overallScore, 3)
        );

        return new AssessmentReportResult(
                summary,
                overallScore,
                items,
                requestPayload,
                writeJson(Map.of(
                        "source", "internal-fallback",
                        "summary", summary,
                        "overallScore", overallScore,
                        "answers", answers
                )),
                true,
                null
        );
    }

    private List<QuestionAnswerPair> pairMessages(List<SessionMessage> messages) {
        List<QuestionAnswerPair> result = new ArrayList<>();
        int index = 1;
        for (int i = 0; i < messages.size(); i++) {
            SessionMessage current = messages.get(i);
            if (current.getSenderType() != SenderType.INTERVIEWER || current.getMessageType() != MessageType.QUESTION) {
                continue;
            }
            String answer = "";
            if (i + 1 < messages.size()) {
                SessionMessage next = messages.get(i + 1);
                if (next.getSenderType() == SenderType.USER && next.getMessageType() == MessageType.ANSWER) {
                    answer = next.getContent();
                }
            }
            result.add(new QuestionAnswerPair(index++, current.getContent(), answer, current.getCreatedAt()));
        }
        return result;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
    }

    private String toSpecialization(InterviewDirection direction) {
        return direction.name().toLowerCase(Locale.ROOT);
    }

    private String toGrade(InterviewLevel level) {
        return level.name().toLowerCase(Locale.ROOT);
    }

    private String normalizeTopicCode(String source) {
        String normalized = source == null ? "general" : source.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? "general" : normalized;
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize assessment payload", exception);
        }
    }

    public record NextPromptResult(
            SenderType senderType,
            MessageType messageType,
            String content,
            boolean advancesQuestionIndex,
            String requestPayload,
            String responsePayload,
            boolean externalRequestSucceeded,
            String errorMessage
    ) {
    }

    public record AssessmentReportResult(
            String summary,
            BigDecimal overallScore,
            List<AssessmentReportItemDraft> items,
            String requestPayload,
            String responsePayload,
            boolean externalRequestSucceeded,
            String errorMessage
    ) {
    }

    public record AssessmentReportItemDraft(
            ReportItemType itemType,
            String title,
            String content,
            BigDecimal score,
            int sortOrder
    ) {
    }

    private record QuestionAnswerPair(
            int index,
            String questionText,
            String answerText,
            LocalDateTime askedAt
    ) {
    }
}
