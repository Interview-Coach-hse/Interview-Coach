package interview.coach.service;

import interview.coach.api.dto.ProgressDtos.ProgressPoint;
import interview.coach.api.dto.ProgressDtos.ProgressResponse;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.repository.InterviewSessionRepository;
import interview.coach.repository.SessionReportRepository;
import interview.coach.security.AppUserPrincipal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    public ProgressResponse getProgress(
            AppUserPrincipal principal,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            InterviewDirection direction,
            InterviewLevel level
    ) {
        var user = userService.getCurrentUser(principal);
        var sessions = interviewSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(session -> matchesFilters(session, createdFrom, createdTo, direction, level))
                .toList();
        long finishedSessions = sessions.stream().filter(session -> session.getState() == SessionState.FINISHED).count();
        List<ProgressPoint> scoreTrend = sessions.stream()
                .map(session -> sessionReportRepository.findBySessionId(session.getId())
                        .filter(report -> report.getStatus() == ReportStatus.READY && report.getOverallScore() != null)
                        .map(report -> new ProgressPoint(
                                session.getId(),
                                session.getCreatedAt(),
                                report.getOverallScore(),
                                session.getDirectionSnapshot(),
                                session.getLevelSnapshot(),
                                report.getScoreSource()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProgressPoint::createdAt))
                .toList();
        List<BigDecimal> scores = scoreTrend.stream().map(ProgressPoint::score).toList();

        BigDecimal average = scores.isEmpty()
                ? null
                : scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        long reportsReady = sessions.stream()
                .filter(session -> sessionReportRepository.findBySessionId(session.getId()).map(report -> report.getStatus() == ReportStatus.READY).orElse(false))
                .count();

        BigDecimal latestScore = scoreTrend.isEmpty() ? null : scoreTrend.get(scoreTrend.size() - 1).score();
        BigDecimal previousScore = scoreTrend.size() < 2 ? null : scoreTrend.get(scoreTrend.size() - 2).score();
        BigDecimal scoreDelta = latestScore == null || previousScore == null
                ? null
                : latestScore.subtract(previousScore).setScale(2, RoundingMode.HALF_UP);

        return new ProgressResponse(
                sessions.size(),
                finishedSessions,
                average,
                reportsReady,
                latestScore,
                previousScore,
                scoreDelta,
                resolveTrend(scoreDelta, latestScore),
                scoreTrend
        );
    }

    private boolean matchesFilters(
            InterviewSession session,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            InterviewDirection direction,
            InterviewLevel level
    ) {
        if (createdFrom != null && session.getCreatedAt().isBefore(createdFrom)) {
            return false;
        }
        if (createdTo != null && session.getCreatedAt().isAfter(createdTo)) {
            return false;
        }
        if (direction != null && session.getDirectionSnapshot() != direction) {
            return false;
        }
        return level == null || session.getLevelSnapshot() == level;
    }

    private String resolveTrend(BigDecimal scoreDelta, BigDecimal latestScore) {
        if (latestScore == null || scoreDelta == null) {
            return "NO_DATA";
        }
        int comparison = scoreDelta.compareTo(BigDecimal.ZERO);
        if (comparison > 0) {
            return "UP";
        }
        if (comparison < 0) {
            return "DOWN";
        }
        return "STABLE";
    }
}
