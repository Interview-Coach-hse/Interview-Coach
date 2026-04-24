package interview.coach.service;

import interview.coach.config.AssessmentClientProperties;
import interview.coach.domain.DomainEnums.MessageType;
import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.ScoreSource;
import interview.coach.domain.DomainEnums.SenderType;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.repository.InterviewSessionRepository;
import interview.coach.repository.SessionMessageRepository;
import interview.coach.repository.SessionReportRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class BusinessMetricsService {

    private static final Logger log = LoggerFactory.getLogger(BusinessMetricsService.class);

    private final InterviewSessionRepository interviewSessionRepository;
    private final SessionMessageRepository sessionMessageRepository;
    private final SessionReportRepository sessionReportRepository;
    private final AssessmentClientProperties assessmentProperties;
    private final RestTemplate assessmentProbeRestTemplate;

    private final AtomicLong sessionsInProgress = new AtomicLong();
    private final AtomicLong sessionsProcessingReport = new AtomicLong();
    private final AtomicLong usersWithActiveSessions = new AtomicLong();
    private final AtomicReference<Double> sessionCompletionRate = new AtomicReference<>(0.0);
    private final AtomicReference<Double> sessionDropoffRate = new AtomicReference<>(0.0);
    private final AtomicReference<Double> averageAnswersPerFinishedSession = new AtomicReference<>(0.0);
    private final AtomicReference<Double> reportGenerationAverageSeconds = new AtomicReference<>(0.0);
    private final AtomicLong reportGenerationFailedTotal = new AtomicLong();
    private final AtomicReference<Double> reportFallbackRate = new AtomicReference<>(0.0);

    private final AtomicLong assessmentProbeUp = new AtomicLong();
    private final AtomicReference<Double> assessmentProbeLatencyMillis = new AtomicReference<>(0.0);
    private final AtomicLong assessmentProbeLastSuccessTimestamp = new AtomicLong();
    private final AtomicLong assessmentProbeConsecutiveFailures = new AtomicLong();

    public BusinessMetricsService(
            MeterRegistry meterRegistry,
            InterviewSessionRepository interviewSessionRepository,
            SessionMessageRepository sessionMessageRepository,
            SessionReportRepository sessionReportRepository,
            AssessmentClientProperties assessmentProperties
    ) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.sessionMessageRepository = sessionMessageRepository;
        this.sessionReportRepository = sessionReportRepository;
        this.assessmentProperties = assessmentProperties;
        this.assessmentProbeRestTemplate = buildAssessmentProbeRestTemplate(assessmentProperties);
        registerMeters(meterRegistry);
    }

    @Scheduled(
            fixedDelayString = "${app.metrics.refresh-interval-millis:60000}",
            initialDelayString = "10000"
    )
    public void refreshBusinessMetrics() {
        long inProgress = interviewSessionRepository.countByState(SessionState.IN_PROGRESS);
        long processing = interviewSessionRepository.countByState(SessionState.PROCESSING);
        long activeUsers = interviewSessionRepository.countDistinctUsersByStateIn(Set.of(SessionState.IN_PROGRESS, SessionState.PROCESSING));
        long finished = interviewSessionRepository.countByState(SessionState.FINISHED);
        long canceled = interviewSessionRepository.countByState(SessionState.CANCELED);
        long failedSessions = interviewSessionRepository.countByState(SessionState.FAILED);
        long terminalSessions = finished + canceled + failedSessions;

        long finishedAnswers = sessionMessageRepository.countBySessionStateAndSenderTypeAndMessageType(
                SessionState.FINISHED,
                SenderType.USER,
                MessageType.ANSWER
        );

        long failedReports = sessionReportRepository.countByStatus(ReportStatus.FAILED);
        long readyReports = sessionReportRepository.countByStatus(ReportStatus.READY);
        long fallbackReports = sessionReportRepository.countByStatusAndScoreSource(ReportStatus.READY, ScoreSource.FALLBACK);
        Double averageReportSeconds = sessionReportRepository.findAverageReadyReportGenerationSeconds();

        sessionsInProgress.set(inProgress);
        sessionsProcessingReport.set(processing);
        usersWithActiveSessions.set(activeUsers);
        sessionCompletionRate.set(rate(finished, terminalSessions));
        sessionDropoffRate.set(rate(canceled + failedSessions, terminalSessions));
        averageAnswersPerFinishedSession.set(finished == 0 ? 0.0 : finishedAnswers / (double) finished);
        reportGenerationAverageSeconds.set(averageReportSeconds == null ? 0.0 : averageReportSeconds);
        reportGenerationFailedTotal.set(failedReports);
        reportFallbackRate.set(rate(fallbackReports, readyReports));
    }

    @Scheduled(
            fixedDelayString = "${app.assessment.probe-interval-millis:60000}",
            initialDelayString = "10000"
    )
    public void refreshAssessmentProbe() {
        if (!assessmentProperties.enabled()) {
            assessmentProbeUp.set(0);
            assessmentProbeLatencyMillis.set(0.0);
            return;
        }

        long startedAt = System.currentTimeMillis();
        try {
            RequestEntity<Void> request = RequestEntity.get(URI.create(assessmentProperties.baseUrl() + "/assessment/v1/health"))
                    .headers(headers -> {
                        if (assessmentProperties.apiKey() != null && !assessmentProperties.apiKey().isBlank()) {
                            headers.add("X-API-Key", assessmentProperties.apiKey());
                        }
                    })
                    .build();

            ResponseEntity<String> response = assessmentProbeRestTemplate.exchange(request, String.class);
            long latency = System.currentTimeMillis() - startedAt;
            assessmentProbeLatencyMillis.set((double) latency);
            if (response.getStatusCode().is2xxSuccessful()) {
                assessmentProbeUp.set(1);
                assessmentProbeLastSuccessTimestamp.set(System.currentTimeMillis() / 1000L);
                assessmentProbeConsecutiveFailures.set(0);
                return;
            }

            markProbeFailure("Assessment health probe returned non-success status: " + response.getStatusCode().value());
        } catch (RestClientException exception) {
            markProbeFailure(exception.getMessage());
        }
    }

    private void markProbeFailure(String reason) {
        assessmentProbeUp.set(0);
        assessmentProbeConsecutiveFailures.incrementAndGet();
        log.warn("Assessment health probe failed: {}", reason);
    }

    private void registerMeters(MeterRegistry meterRegistry) {
        Gauge.builder("interview_sessions_in_progress", sessionsInProgress, AtomicLong::get)
                .description("Current number of interview sessions in progress")
                .register(meterRegistry);
        Gauge.builder("interview_sessions_processing_report", sessionsProcessingReport, AtomicLong::get)
                .description("Current number of interview sessions waiting for report generation")
                .register(meterRegistry);
        Gauge.builder("interview_users_with_active_sessions", usersWithActiveSessions, AtomicLong::get)
                .description("Distinct users who currently have active or processing interview sessions")
                .register(meterRegistry);
        Gauge.builder("interview_session_completion_rate", sessionCompletionRate, AtomicReference::get)
                .description("Share of terminal sessions completed successfully")
                .register(meterRegistry);
        Gauge.builder("interview_session_dropoff_rate", sessionDropoffRate, AtomicReference::get)
                .description("Share of terminal sessions that ended canceled or failed")
                .register(meterRegistry);
        Gauge.builder("interview_average_answers_per_finished_session", averageAnswersPerFinishedSession, AtomicReference::get)
                .description("Average number of user answers per finished session")
                .register(meterRegistry);
        Gauge.builder("interview_report_generation_average_seconds", reportGenerationAverageSeconds, AtomicReference::get)
                .description("Average report generation time in seconds for ready reports")
                .register(meterRegistry);
        Gauge.builder("interview_report_generation_failed_total", reportGenerationFailedTotal, AtomicLong::get)
                .description("Total number of failed report generations")
                .register(meterRegistry);
        Gauge.builder("interview_report_fallback_rate", reportFallbackRate, AtomicReference::get)
                .description("Share of ready reports generated by fallback logic")
                .register(meterRegistry);

        Gauge.builder("interview_assessment_probe_up", assessmentProbeUp, AtomicLong::get)
                .description("Assessment health probe status, 1 when healthy and 0 otherwise")
                .register(meterRegistry);
        Gauge.builder("interview_assessment_probe_latency_ms", assessmentProbeLatencyMillis, AtomicReference::get)
                .description("Latency of the assessment health probe in milliseconds")
                .register(meterRegistry);
        Gauge.builder("interview_assessment_probe_last_success_timestamp", assessmentProbeLastSuccessTimestamp, AtomicLong::get)
                .description("Unix timestamp of the last successful assessment health probe")
                .register(meterRegistry);
        Gauge.builder("interview_assessment_probe_consecutive_failures", assessmentProbeConsecutiveFailures, AtomicLong::get)
                .description("Consecutive failed assessment health probes")
                .register(meterRegistry);
    }

    private RestTemplate buildAssessmentProbeRestTemplate(AssessmentClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(properties.probeTimeoutMillis());
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);
        return new RestTemplate(requestFactory);
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return numerator / (double) denominator;
    }
}
