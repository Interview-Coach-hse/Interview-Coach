package interview.coach.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record EmailVerificationRequest(
            @Email @NotBlank String email
    ) {
    }

    public record EmailVerificationConfirmRequest(
            @Email @NotBlank String email,
            @NotBlank String code
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record PasswordResetRequest(
            @Email @NotBlank String email
    ) {
    }

    public record PasswordResetConfirmRequest(
            @NotBlank String token,
            @NotBlank String newPassword
    ) {
    }

    public record PasswordResetResponse(
            String message,
            String resetToken
    ) {
    }

    public record VerificationResponse(
            String message,
            String email,
            String code
    ) {
    }

    public record RefreshTokenRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UserDtos.UserResponse user
    ) {
    }
}
