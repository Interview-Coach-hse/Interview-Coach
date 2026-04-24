package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public final class QuestionDtos {

    private QuestionDtos() {
    }

    public record QuestionRequest(
            @NotBlank String text,
            @NotNull QuestionType questionType,
            String difficulty,
            String direction,
            QuestionStatus status
    ) {
    }

    public record QuestionResponse(
            UUID id,
            String text,
            QuestionType questionType,
            String difficulty,
            String direction,
            QuestionStatus status,
            UUID createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record QuestionPageResponse(
            List<QuestionResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static QuestionPageResponse from(Page<QuestionResponse> page) {
            return new QuestionPageResponse(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
        }
    }
}
