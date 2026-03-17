package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ProfileStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record ProfileRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank String description,
            @NotNull InterviewDirection direction,
            @NotNull InterviewLevel level,
            List<String> tags
    ) {
    }

    public record ProfileResponse(
            UUID id,
            String title,
            String description,
            InterviewDirection direction,
            InterviewLevel level,
            ProfileStatus status,
            List<String> tags,
            List<ProfileQuestionResponse> questions,
            LocalDateTime publishedAt
    ) {
    }

    public record ProfileQuestionRequest(
            @NotNull UUID questionId,
            @NotNull Integer orderIndex,
            Boolean required
    ) {
    }

    public record ProfileQuestionResponse(
            UUID id,
            UUID questionId,
            String questionText,
            String questionType,
            Integer orderIndex,
            boolean required
    ) {
    }
}
