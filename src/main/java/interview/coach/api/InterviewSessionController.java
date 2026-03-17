package interview.coach.api;

import interview.coach.api.dto.SessionDtos.CreateSessionRequest;
import interview.coach.api.dto.PageDtos.PageResponse;
import interview.coach.api.dto.SessionDtos.ReportResponse;
import interview.coach.api.dto.SessionDtos.SendMessageRequest;
import interview.coach.api.dto.SessionDtos.SendMessageResponse;
import interview.coach.api.dto.SessionDtos.SessionMessageResponse;
import interview.coach.api.dto.SessionDtos.SessionResponse;
import interview.coach.api.dto.SessionDtos.SessionStateResponse;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.InterviewSessionService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    public InterviewSessionController(InterviewSessionService interviewSessionService) {
        this.interviewSessionService = interviewSessionService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody CreateSessionRequest request
    ) {
        return ResponseEntity.ok(interviewSessionService.createSession(principal, request));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getById(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.getSession(principal, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<SessionStateResponse> start(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.startSession(principal, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/pause")
    public ResponseEntity<SessionStateResponse> pause(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.pauseSession(principal, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/resume")
    public ResponseEntity<SessionStateResponse> resume(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.resumeSession(principal, sessionId));
    }

    @PostMapping("/sessions/{sessionId}/cancel")
    public ResponseEntity<SessionStateResponse> cancel(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.cancelSession(principal, sessionId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<PageResponse<SessionMessageResponse>> messages(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(interviewSessionService.getMessages(principal, sessionId, page, size));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<SendMessageResponse> sendMessage(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return ResponseEntity.ok(interviewSessionService.sendMessage(principal, sessionId, request));
    }

    @PostMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<SessionResponse> finish(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.finishSession(principal, sessionId));
    }

    @GetMapping("/sessions/{sessionId}/report")
    public ResponseEntity<ReportResponse> report(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(interviewSessionService.getReport(principal, sessionId));
    }

    @GetMapping("/history/sessions")
    public ResponseEntity<PageResponse<SessionResponse>> history(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) interview.coach.domain.DomainEnums.SessionState state,
            @RequestParam(required = false) UUID profileId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(interviewSessionService.getHistory(principal, state, profileId, createdFrom, createdTo, page, size));
    }
}
