package interview.coach.api.dto;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class AdminUserDtos {

    private AdminUserDtos() {
    }

    public record AdminUserUpdateRequest(
            @Email String email,
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            UserStatus status,
            String roleCode,
            InterviewDirection preferredDirection,
            InterviewLevel preferredLevel,
            @Size(max = 30) String preferredLanguage,
            @Size(max = 10) String interfaceLanguage,
            @Size(max = 20) String theme
    ) {
    }

    public record AdminUserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            UserStatus status,
            String roleCode,
            UserDtos.PreferenceResponse preference
    ) {
    }
}
