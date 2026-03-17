package interview.coach.service;

import interview.coach.api.dto.ProgressDtos.ProgressResponse;
import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.repository.InterviewSessionRepository;
import interview.coach.repository.SessionReportRepository;
import interview.coach.security.AppUserPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final SessionReportRepository sessionReportRepository;
    private final UserService userService;

    public ProgressService(
            InterviewSessionRepository interviewSessionRepository,
            SessionReportRepository sessionReportRepository,
            UserService userService
    ) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.sessionReportRepository = sessionReportRepository;
        this.userService = userService;
    }

    public ProgressResponse getProgress(AppUserPrincipal principal) {
        var user = userService.getCurrentUser(principal);
        var sessions = interviewSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long finishedSessions = sessions.stream().filter(session -> session.getState() == SessionState.FINISHED || session.getState() == SessionState.REPORT_READY).count();
        List<BigDecimal> scores = sessions.stream()
                .map(session -> sessionReportRepository.findBySessionId(session.getId()).orElse(null))
                .filter(report -> report != null && report.getStatus() == ReportStatus.READY && report.getOverallScore() != null)
                .map(report -> report.getOverallScore())
                .toList();

        BigDecimal average = scores.isEmpty()
                ? null
                : scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        long reportsReady = sessions.stream()
                .filter(session -> sessionReportRepository.findBySessionId(session.getId()).map(report -> report.getStatus() == ReportStatus.READY).orElse(false))
                .count();

        return new ProgressResponse(sessions.size(), finishedSessions, average, reportsReady);
    }
}
