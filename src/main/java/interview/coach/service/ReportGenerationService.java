package interview.coach.service;

import interview.coach.domain.DomainEnums.ExternalRequestStatus;
import interview.coach.domain.DomainEnums.ExternalRequestType;
import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.ExternalRequest;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.domain.entity.ReportItem;
import interview.coach.domain.entity.SessionReport;
import interview.coach.repository.ExternalRequestRepository;
import interview.coach.repository.ReportItemRepository;
import interview.coach.repository.SessionMessageRepository;
import interview.coach.repository.SessionReportRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationService.class);

    private final ExternalRequestRepository externalRequestRepository;
    private final SessionReportRepository sessionReportRepository;
    private final ReportItemRepository reportItemRepository;
    private final SessionMessageRepository sessionMessageRepository;
    private final AssessmentAiService assessmentAiService;

    public ReportGenerationService(
            ExternalRequestRepository externalRequestRepository,
            SessionReportRepository sessionReportRepository,
            ReportItemRepository reportItemRepository,
            SessionMessageRepository sessionMessageRepository,
            AssessmentAiService assessmentAiService
    ) {
        this.externalRequestRepository = externalRequestRepository;
        this.sessionReportRepository = sessionReportRepository;
        this.reportItemRepository = reportItemRepository;
        this.sessionMessageRepository = sessionMessageRepository;
        this.assessmentAiService = assessmentAiService;
    }

    @Transactional
    public void generateFor(InterviewSession session) {
        LocalDateTime now = LocalDateTime.now();
        ExternalRequest externalRequest = new ExternalRequest();
        externalRequest.setSession(session);
        externalRequest.setRequestType(ExternalRequestType.FINAL_REPORT);
        externalRequest.setRequestStatus(ExternalRequestStatus.SENT);
        externalRequest.setAttemptCount(1);
        externalRequest.setCreatedAt(now);
        externalRequest.setSentAt(now);

        SessionReport report = sessionReportRepository.findBySessionId(session.getId()).orElseGet(() -> {
            SessionReport created = new SessionReport();
            created.setSession(session);
            created.setStatus(ReportStatus.PENDING);
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            created.setRequestedAt(now);
            return created;
        });

        try {
            var messages = sessionMessageRepository.findBySessionIdOrderBySequenceNumberAsc(
                    session.getId(),
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();

            var draft = assessmentAiService.generateReport(session, messages);
            externalRequest.setRequestPayload(draft.requestPayload());
            externalRequest.setResponsePayload(draft.responsePayload());
            externalRequest.setRequestStatus(draft.externalRequestSucceeded() ? ExternalRequestStatus.SUCCESS : ExternalRequestStatus.FAILED);
            externalRequest.setErrorMessage(draft.errorMessage());
            externalRequest.setCompletedAt(LocalDateTime.now());
            externalRequestRepository.save(externalRequest);

            report.setExternalRequest(externalRequest);
            report.setStatus(ReportStatus.READY);
            report.setSummaryText(draft.summary());
            report.setOverallScore(draft.overallScore());
            report.setRawPayload(externalRequest.getResponsePayload());
            report.setGeneratedAt(LocalDateTime.now());
            report.setErrorMessage(draft.externalRequestSucceeded() ? null : draft.errorMessage());
            report.setUpdatedAt(LocalDateTime.now());
            SessionReport savedReport = sessionReportRepository.save(report);

            reportItemRepository.deleteByReport_Id(savedReport.getId());
            reportItemRepository.saveAll(draft.items().stream().map(item -> toEntity(savedReport, item)).toList());

            session.setState(SessionState.REPORT_READY);
            session.setUpdatedAt(LocalDateTime.now());
        } catch (Exception exception) {
            log.error("Failed to generate report for session {}", session.getId(), exception);
            externalRequest.setRequestStatus(ExternalRequestStatus.FAILED);
            externalRequest.setErrorMessage(exception.getMessage());
            externalRequest.setCompletedAt(LocalDateTime.now());
            externalRequestRepository.save(externalRequest);

            report.setExternalRequest(externalRequest);
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(exception.getMessage());
            report.setUpdatedAt(LocalDateTime.now());
            sessionReportRepository.save(report);

            session.setState(SessionState.FAILED);
            session.setLastErrorCode("REPORT_GENERATION_FAILED");
            session.setLastErrorMessage(exception.getMessage());
            session.setUpdatedAt(LocalDateTime.now());
        }
    }

    private ReportItem toEntity(SessionReport report, AssessmentAiService.AssessmentReportItemDraft draft) {
        ReportItem entity = new ReportItem();
        entity.setReport(report);
        entity.setItemType(draft.itemType());
        entity.setTitle(draft.title());
        entity.setContent(draft.content());
        entity.setScore(draft.score());
        entity.setSortOrder(draft.sortOrder());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
