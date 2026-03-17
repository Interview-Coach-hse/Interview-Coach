package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.UserStatus;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {
    }

    public record UpdateUserRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            InterviewDirection preferredDirection,
            InterviewLevel preferredLevel,
            @Size(max = 30) String preferredLanguage,
            @Size(max = 10) String interfaceLanguage,
            @Size(max = 20) String theme
    ) {
    }

    public record UserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            UserStatus status,
            String role,
            PreferenceResponse preference
    ) {
    }

    public record PreferenceResponse(
            InterviewDirection preferredDirection,
            InterviewLevel preferredLevel,
            String preferredLanguage,
            String interfaceLanguage,
            String theme
    ) {
    }
}
