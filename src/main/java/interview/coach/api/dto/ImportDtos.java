package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import java.util.List;
import java.util.UUID;

public final class ImportDtos {

    private ImportDtos() {
    }

    public record ImportQuestionPayload(
            String text,
            QuestionType questionType,
            InterviewLevel difficulty,
            InterviewDirection direction,
            QuestionStatus status,
            Integer orderIndex,
            Boolean required
    ) {
    }

    public record ImportProfilePayload(
            String title,
            String description,
            InterviewDirection direction,
            InterviewLevel level,
            List<String> tags
    ) {
    }

    public record ImportRequest(
            ImportProfilePayload profile,
            List<ImportQuestionPayload> questions
    ) {
    }

    public record ImportResponse(
            String mode,
            UUID profileId,
            String profileTitle,
            int totalQuestions,
            int createdQuestions,
            int reusedQuestions,
            int linkedQuestions
    ) {
    }
}
