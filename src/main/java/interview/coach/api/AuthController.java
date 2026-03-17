package interview.coach.api;

import interview.coach.api.dto.AuthDtos.AuthResponse;
import interview.coach.api.dto.AuthDtos.EmailVerificationConfirmRequest;
import interview.coach.api.dto.AuthDtos.EmailVerificationRequest;
import interview.coach.api.dto.AuthDtos.LoginRequest;
import interview.coach.api.dto.AuthDtos.PasswordResetConfirmRequest;
import interview.coach.api.dto.AuthDtos.PasswordResetRequest;
import interview.coach.api.dto.AuthDtos.PasswordResetResponse;
import interview.coach.api.dto.AuthDtos.RefreshTokenRequest;
import interview.coach.api.dto.AuthDtos.RegisterRequest;
import interview.coach.api.dto.AuthDtos.VerificationResponse;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<VerificationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.accepted().body(authService.register(request));
    }

    @PostMapping("/verify-email/request")
    public ResponseEntity<VerificationResponse> resendVerification(@Valid @RequestBody EmailVerificationRequest request) {
        return ResponseEntity.accepted().body(authService.resendVerificationCode(request));
    }

    @PostMapping("/verify-email/confirm")
    public ResponseEntity<Void> confirmVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        authService.confirmEmail(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(principal, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<PasswordResetResponse> passwordReset(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.accepted().body(authService.requestPasswordReset(request.email()));
    }

    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Void> passwordResetConfirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.noContent().build();
    }
}
