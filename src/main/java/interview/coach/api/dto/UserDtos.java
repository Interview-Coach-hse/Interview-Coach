package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.UserStatus;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {
    }

    public record UpdateUserRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            String preferredDirection,
            String preferredLevel,
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
            String preferredDirection,
            String preferredLevel,
            String preferredLanguage,
            String interfaceLanguage,
            String theme
    ) {
    }
}
