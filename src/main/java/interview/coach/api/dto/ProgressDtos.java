package interview.coach.api.dto;

import java.math.BigDecimal;

public final class ProgressDtos {

    private ProgressDtos() {
    }

    public record ProgressResponse(
            long totalSessions,
            long finishedSessions,
            BigDecimal averageScore,
            long reportsReady
    ) {
    }
}
