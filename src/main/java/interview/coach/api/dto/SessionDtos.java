package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.MessageType;
import interview.coach.domain.DomainEnums.ReportItemType;
import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.SenderType;
import interview.coach.domain.DomainEnums.SessionState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class SessionDtos {

    private SessionDtos() {
    }

    public record CreateSessionRequest(
            @NotNull UUID profileId
    ) {
    }

    public record SessionStateResponse(
            SessionResponse session,
            String action
    ) {
    }

    public record SendMessageRequest(
            @NotBlank
            @Size(max = 4000)
            String message
    ) {
    }

    public record SessionResponse(
            UUID id,
            UUID profileId,
            String profileTitle,
            SessionState state,
            Integer currentQuestionIndex,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            Long durationSeconds
    ) {
    }

    public record SessionMessageResponse(
            UUID id,
            SenderType senderType,
            MessageType messageType,
            String content,
            int sequenceNumber,
            LocalDateTime createdAt
    ) {
    }

    public record SendMessageResponse(
            SessionMessageResponse userMessage,
            SessionMessageResponse systemReply
    ) {
    }

    public record ReportResponse(
            ReportStatus status,
            String summaryText,
            BigDecimal overallScore,
            List<ReportItemResponse> items
    ) {
    }

    public record ReportItemResponse(
            ReportItemType itemType,
            String title,
            String content,
            BigDecimal score,
            int sortOrder
    ) {
    }

    public record HistoryResponse(
            List<SessionResponse> sessions
    ) {
    }

    public static Long durationSeconds(LocalDateTime startedAt, LocalDateTime finishedAt) {
        if (startedAt == null) {
            return null;
        }
        LocalDateTime end = finishedAt == null ? LocalDateTime.now() : finishedAt;
        return Duration.between(startedAt, end).getSeconds();
    }
}
