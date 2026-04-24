package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.ScoreSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ProgressDtos {

    private ProgressDtos() {
    }

    public record ProgressPoint(
            UUID sessionId,
            LocalDateTime createdAt,
            BigDecimal score,
            String direction,
            String level,
            ScoreSource scoreSource
    ) {
    }

    public record ProgressResponse(
            long totalSessions,
            long finishedSessions,
            BigDecimal averageScore,
            long reportsReady,
            BigDecimal latestScore,
            BigDecimal previousScore,
            BigDecimal scoreDelta,
            String trend,
            List<ProgressPoint> scoreTrend
    ) {
    }
}
