package interview.coach.service;

import interview.coach.api.dto.SessionDtos.CreateSessionRequest;
import interview.coach.api.dto.PageDtos.PageResponse;
import interview.coach.api.dto.SessionDtos.ReportItemResponse;
import interview.coach.api.dto.SessionDtos.ReportResponse;
import interview.coach.api.dto.SessionDtos.SendMessageRequest;
import interview.coach.api.dto.SessionDtos.SendMessageResponse;
import interview.coach.api.dto.SessionDtos.SessionMessageResponse;
import interview.coach.api.dto.SessionDtos.SessionResponse;
import interview.coach.api.dto.SessionDtos.SessionStateResponse;
import interview.coach.domain.DomainEnums.MessageType;
import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.SenderType;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.domain.entity.ExternalRequest;
import interview.coach.domain.entity.ReportItem;
import interview.coach.domain.entity.SessionMessage;
import interview.coach.domain.entity.SessionReport;
import interview.coach.domain.entity.User;
import interview.coach.domain.DomainEnums.ExternalRequestStatus;
import interview.coach.domain.DomainEnums.ExternalRequestType;
import interview.coach.exception.ApiException;
import interview.coach.repository.ExternalRequestRepository;
import interview.coach.repository.InterviewSessionRepository;
import interview.coach.repository.ReportItemRepository;
import interview.coach.repository.SessionMessageRepository;
import interview.coach.repository.SessionReportRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static interview.coach.api.dto.SessionDtos.durationSeconds;

@Service
public class InterviewSessionService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final SessionMessageRepository sessionMessageRepository;
    private final SessionReportRepository sessionReportRepository;
    private final ReportItemRepository reportItemRepository;
    private final ExternalRequestRepository externalRequestRepository;
    private final InterviewProfileService interviewProfileService;
    private final UserService userService;
    private final ReportGenerationService reportGenerationService;
    private final AssessmentAiService assessmentAiService;

    public InterviewSessionService(
            InterviewSessionRepository interviewSessionRepository,
            SessionMessageRepository sessionMessageRepository,
            SessionReportRepository sessionReportRepository,
            ReportItemRepository reportItemRepository,
            ExternalRequestRepository externalRequestRepository,
            InterviewProfileService interviewProfileService,
            UserService userService,
            ReportGenerationService reportGenerationService,
            AssessmentAiService assessmentAiService
    ) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.sessionMessageRepository = sessionMessageRepository;
        this.sessionReportRepository = sessionReportRepository;
        this.reportItemRepository = reportItemRepository;
        this.externalRequestRepository = externalRequestRepository;
        this.interviewProfileService = interviewProfileService;
        this.userService = userService;
        this.reportGenerationService = reportGenerationService;
        this.assessmentAiService = assessmentAiService;
    }

    @Transactional
    public SessionResponse createSession(AppUserPrincipal principal, CreateSessionRequest request) {
        User user = userService.getCurrentUser(principal);
        if (interviewSessionRepository.existsByUserIdAndStateIn(user.getId(), Set.of(SessionState.IN_PROGRESS))) {
            throw new ApiException(HttpStatus.CONFLICT, "User already has an active session in progress");
        }

        InterviewProfile profile = interviewProfileService.requirePublished(request.profileId());
        LocalDateTime now = LocalDateTime.now();

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setProfile(profile);
        session.setState(SessionState.CREATED);
        session.setCurrentQuestionIndex(0);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        interviewSessionRepository.save(session);
        return toResponse(session);
    }

    public SessionResponse getSession(AppUserPrincipal principal, UUID sessionId) {
        return toResponse(requireOwnedSession(principal, sessionId));
    }

    @Transactional
    public SessionStateResponse startSession(AppUserPrincipal principal, UUID sessionId) {
        InterviewSession session = requireOwnedSession(principal, sessionId);
        User user = userService.getCurrentUser(principal);
        if (interviewSessionRepository.existsByUserIdAndStateIn(user.getId(), Set.of(SessionState.IN_PROGRESS))
                && session.getState() != SessionState.IN_PROGRESS) {
            throw new ApiException(HttpStatus.CONFLICT, "User already has an active session in progress");
        }
        if (session.getState() != SessionState.CREATED && session.getState() != SessionState.PAUSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session cannot be started from current state");
        }
        LocalDateTime now = LocalDateTime.now();
        session.setState(SessionState.IN_PROGRESS);
        if (session.getStartedAt() == null) {
            session.setStartedAt(now);
        }
        session.setUpdatedAt(now);
        interviewSessionRepository.save(session);
        if (sessionMessageRepository.countBySessionId(sessionId) == 0) {
            requestAndSaveNextPrompt(session);
            interviewSessionRepository.save(session);
        }
        return new SessionStateResponse(toResponse(session), "STARTED");
    }

    @Transactional
    public SessionStateResponse pauseSession(AppUserPrincipal principal, UUID sessionId) {
        InterviewSession session = requireOwnedSession(principal, sessionId);
        if (session.getState() != SessionState.IN_PROGRESS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only in-progress sessions can be paused");
        }
        session.setState(SessionState.PAUSED);
        session.setUpdatedAt(LocalDateTime.now());
        return new SessionStateResponse(toResponse(interviewSessionRepository.save(session)), "PAUSED");
    }

    @Transactional
    public SessionStateResponse resumeSession(AppUserPrincipal principal, UUID sessionId) {
        InterviewSession session = requireOwnedSession(principal, sessionId);
        User user = userService.getCurrentUser(principal);
        if (session.getState() != SessionState.PAUSED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only paused sessions can be resumed");
        }
        if (interviewSessionRepository.existsByUserIdAndStateIn(user.getId(), Set.of(SessionState.IN_PROGRESS))) {
            throw new ApiException(HttpStatus.CONFLICT, "User already has another session in progress");
        }
        session.setState(SessionState.IN_PROGRESS);
        session.setUpdatedAt(LocalDateTime.now());
        return new SessionStateResponse(toResponse(interviewSessionRepository.save(session)), "RESUMED");
    }

    @Transactional
    public SessionStateResponse cancelSession(AppUserPrincipal principal, UUID sessionId) {
        InterviewSession session = requireOwnedSession(principal, sessionId);
        if (session.getState() == SessionState.FINISHED || session.getState() == SessionState.CANCELED || session.getState() == SessionState.REPORT_READY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session cannot be canceled from current state");
        }
        LocalDateTime now = LocalDateTime.now();
        session.setState(SessionState.CANCELED);
        session.setFinishedAt(session.getFinishedAt() == null ? now : session.getFinishedAt());
        session.setUpdatedAt(now);
        return new SessionStateResponse(toResponse(interviewSessionRepository.save(session)), "CANCELED");
    }

    @Transactional
    public SendMessageResponse sendMessage(AppUserPrincipal principal, UUID sessionId, SendMessageRequest request) {
        InterviewSession session = requireOwnedSession(principal, sessionId);
        if (session.getState() == SessionState.FINISHED || session.getState() == SessionState.CANCELED || session.getState() == SessionState.PROCESSING || session.getState() == SessionState.REPORT_READY || session.getState() == SessionState.PAUSED || session.getState() == SessionState.FAILED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot send message for the current session state");
        }

        if (session.getState() == SessionState.CREATED) {
            session.setState(SessionState.IN_PROGRESS);
            session.setStartedAt(LocalDateTime.now());
        }

        long currentCount = sessionMessageRepository.countBySessionId(sessionId);
        SessionMessage userMessage = saveMessage(session, SenderType.USER, MessageType.ANSWER, request.message(), (int) currentCount);
        SessionMessage systemReply = requestAndSaveNextPrompt(session);
        session.setUpdatedAt(LocalDateTime.now());
        interviewSessionRepository.save(session);
        return new SendMessageResponse(toResponse(userMessage), toResponse(systemReply));
    }

    @Transactional
    public SessionResponse finishSession(AppUserPrincipal principal, UUID sessionId) {
        InterviewSession session = requireOwnedSession(principal, sessionId);
        if (session.getState() != SessionState.IN_PROGRESS && session.getState() != SessionState.PAUSED && session.getState() != SessionState.CREATED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session cannot be finished from current state");
        }

        LocalDateTime now = LocalDateTime.now();
        session.setState(SessionState.PROCESSING);
        session.setFinishedAt(now);
        session.setUpdatedAt(now);
        interviewSessionRepository.save(session);

        sessionReportRepository.findBySessionId(sessionId).orElseGet(() -> {
            SessionReport report = new SessionReport();
            report.setSession(session);
            report.setStatus(ReportStatus.PENDING);
            report.setRequestedAt(now);
            report.setCreatedAt(now);
            report.setUpdatedAt(now);
            return sessionReportRepository.save(report);
        });

        reportGenerationService.generateFor(session);
        interviewSessionRepository.save(session);
        return toResponse(session);
    }

    public ReportResponse getReport(AppUserPrincipal principal, UUID sessionId) {
        requireOwnedSession(principal, sessionId);
        SessionReport report = sessionReportRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    SessionReport pending = new SessionReport();
                    pending.setStatus(ReportStatus.PENDING);
                    return pending;
                });
        List<ReportItemResponse> items = report.getId() == null ? List.of() : reportItemRepository.findByReport_IdOrderBySortOrderAsc(report.getId()).stream()
                .map(this::toResponse)
                .toList();
        return new ReportResponse(report.getStatus(), report.getSummaryText(), report.getOverallScore(), items);
    }

    public PageResponse<SessionResponse> getHistory(
            AppUserPrincipal principal,
            SessionState state,
            UUID profileId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            int page,
            int size
    ) {
        User user = userService.getCurrentUser(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<InterviewSession> specification = Specification.where(hasUser(user.getId()))
                .and(hasState(state))
                .and(hasProfile(profileId))
                .and(createdFrom(createdFrom))
                .and(createdTo(createdTo));
        return PageResponse.from(interviewSessionRepository.findAll(specification, pageable).map(this::toResponse));
    }

    public PageResponse<SessionMessageResponse> getMessages(AppUserPrincipal principal, UUID sessionId, int page, int size) {
        requireOwnedSession(principal, sessionId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("sequenceNumber").ascending());
        return PageResponse.from(sessionMessageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId, pageable).map(this::toResponse));
    }

    private InterviewSession requireOwnedSession(AppUserPrincipal principal, UUID sessionId) {
        return interviewSessionRepository.findByIdAndUserId(sessionId, principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Interview session not found"));
    }

    private SessionMessage saveMessage(InterviewSession session, SenderType senderType, MessageType messageType, String content, int sequenceNumber) {
        SessionMessage message = new SessionMessage();
        message.setSession(session);
        message.setSenderType(senderType);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setSequenceNumber(sequenceNumber);
        message.setCreatedAt(LocalDateTime.now());
        return sessionMessageRepository.save(message);
    }

    private SessionMessage requestAndSaveNextPrompt(InterviewSession session) {
        int nextQuestionIndex = session.getCurrentQuestionIndex() == null ? 0 : session.getCurrentQuestionIndex();
        var prompt = assessmentAiService.getNextPrompt(session, nextQuestionIndex);

        ExternalRequest externalRequest = new ExternalRequest();
        externalRequest.setSession(session);
        externalRequest.setRequestType(ExternalRequestType.NEXT_QUESTION);
        externalRequest.setRequestStatus(prompt.externalRequestSucceeded() ? ExternalRequestStatus.SUCCESS : ExternalRequestStatus.FAILED);
        externalRequest.setRequestPayload(prompt.requestPayload());
        externalRequest.setResponsePayload(prompt.responsePayload());
        externalRequest.setErrorMessage(prompt.errorMessage());
        externalRequest.setAttemptCount(1);
        externalRequest.setCreatedAt(LocalDateTime.now());
        externalRequest.setSentAt(LocalDateTime.now());
        externalRequest.setCompletedAt(LocalDateTime.now());
        externalRequestRepository.save(externalRequest);

        int sequenceNumber = (int) sessionMessageRepository.countBySessionId(session.getId());
        SessionMessage promptMessage = saveMessage(session, prompt.senderType(), prompt.messageType(), prompt.content(), sequenceNumber);
        if (prompt.advancesQuestionIndex()) {
            session.setCurrentQuestionIndex(nextQuestionIndex + 1);
        }
        return promptMessage;
    }

    public SessionResponse toResponse(InterviewSession session) {
        return new SessionResponse(
                session.getId(),
                session.getProfile().getId(),
                session.getProfile().getTitle(),
                session.getState(),
                session.getCurrentQuestionIndex(),
                session.getStartedAt(),
                session.getFinishedAt(),
                durationSeconds(session.getStartedAt(), session.getFinishedAt())
        );
    }

    private SessionMessageResponse toResponse(SessionMessage message) {
        return new SessionMessageResponse(
                message.getId(),
                message.getSenderType(),
                message.getMessageType(),
                message.getContent(),
                message.getSequenceNumber(),
                message.getCreatedAt()
        );
    }

    private ReportItemResponse toResponse(ReportItem item) {
        return new ReportItemResponse(item.getItemType(), item.getTitle(), item.getContent(), item.getScore(), item.getSortOrder());
    }

    private Specification<InterviewSession> hasUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    private Specification<InterviewSession> hasState(SessionState state) {
        if (state == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    private Specification<InterviewSession> hasProfile(UUID profileId) {
        if (profileId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("profile").get("id"), profileId);
    }

    private Specification<InterviewSession> createdFrom(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private Specification<InterviewSession> createdTo(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
