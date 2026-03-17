package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public final class QuestionDtos {

    private QuestionDtos() {
    }

    public record QuestionRequest(
            @NotBlank String text,
            @NotNull QuestionType questionType,
            InterviewLevel difficulty,
            InterviewDirection direction,
            QuestionStatus status
    ) {
    }

    public record QuestionResponse(
            UUID id,
            String text,
            QuestionType questionType,
            InterviewLevel difficulty,
            InterviewDirection direction,
            QuestionStatus status,
            UUID createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
