package interview.coach.api;

import interview.coach.generated.api.AuthApi;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.VerificationResponse> authRegisterPost(
            @Valid @RequestBody interview.coach.generated.model.RegisterRequest request
    ) {
        return ResponseEntity.accepted().body(GeneratedApiSupport.toGenerated(authService.register(GeneratedApiSupport.toInternal(request))));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.VerificationResponse> authVerifyEmailRequestPost(
            @Valid @RequestBody interview.coach.generated.model.EmailVerificationRequest request
    ) {
        return ResponseEntity.accepted().body(GeneratedApiSupport.toGenerated(authService.resendVerificationCode(GeneratedApiSupport.toInternal(request))));
    }

    @Override
    public ResponseEntity<Void> authVerifyEmailConfirmPost(
            @Valid @RequestBody interview.coach.generated.model.EmailVerificationConfirmRequest request
    ) {
        authService.confirmEmail(GeneratedApiSupport.toInternal(request));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.AuthResponse> authLoginPost(
            @Valid @RequestBody interview.coach.generated.model.LoginRequest request
    ) {
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(authService.login(GeneratedApiSupport.toInternal(request))));
    }

    @Override
    public ResponseEntity<Void> authLogoutPost(
            @Valid @RequestBody interview.coach.generated.model.RefreshTokenRequest request
    ) {
        AppUserPrincipal principal = GeneratedApiSupport.currentPrincipal();
        authService.logout(principal, GeneratedApiSupport.toInternal(request));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.AuthResponse> authRefreshPost(
            @Valid @RequestBody interview.coach.generated.model.RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(authService.refresh(GeneratedApiSupport.toInternal(request))));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.PasswordResetResponse> authPasswordResetPost(
            @Valid @RequestBody interview.coach.generated.model.PasswordResetRequest request
    ) {
        return ResponseEntity.accepted().body(GeneratedApiSupport.toGenerated(authService.requestPasswordReset(request.getEmail())));
    }

    @Override
    public ResponseEntity<Void> authPasswordResetConfirmPost(
            @Valid @RequestBody interview.coach.generated.model.PasswordResetConfirmRequest request
    ) {
        authService.confirmPasswordReset(GeneratedApiSupport.toInternal(request));
        return ResponseEntity.noContent().build();
    }
}
