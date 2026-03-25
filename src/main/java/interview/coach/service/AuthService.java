package interview.coach.service;

import interview.coach.api.dto.AuthDtos.AuthResponse;
import interview.coach.api.dto.AuthDtos.EmailVerificationConfirmRequest;
import interview.coach.api.dto.AuthDtos.EmailVerificationRequest;
import interview.coach.api.dto.AuthDtos.LoginRequest;
import interview.coach.api.dto.AuthDtos.PasswordResetConfirmRequest;
import interview.coach.api.dto.AuthDtos.RegisterRequest;
import interview.coach.api.dto.AuthDtos.PasswordResetResponse;
import interview.coach.api.dto.AuthDtos.RefreshTokenRequest;
import interview.coach.api.dto.AuthDtos.VerificationResponse;
import interview.coach.domain.DomainEnums.UserStatus;
import interview.coach.domain.entity.PasswordResetToken;
import interview.coach.domain.entity.Role;
import interview.coach.domain.entity.User;
import interview.coach.domain.entity.UserPreference;
import interview.coach.exception.ApiException;
import interview.coach.repository.PasswordResetTokenRepository;
import interview.coach.repository.RoleRepository;
import interview.coach.repository.UserPreferenceRepository;
import interview.coach.repository.UserRepository;
import interview.coach.security.AppUserPrincipal;
import interview.coach.security.JwtService;
import interview.coach.security.RefreshTokenService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final boolean mailEnabled;

    public AuthService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserService userService,
            EmailVerificationService emailVerificationService,
            EmailService emailService,
            @org.springframework.beans.factory.annotation.Value("${app.mail.enabled}") boolean mailEnabled
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
        this.mailEnabled = mailEnabled;
    }

    @Transactional
    public VerificationResponse register(RegisterRequest request) {
        log.info("Register request received for email={}", request.email());
        validatePassword(request.password());
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            log.warn("Registration rejected, email already exists: {}", request.email());
            throw new ApiException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        Role role = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Default role USER not found"));

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setRole(role);
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setInterfaceLanguage("ru");
        preference.setTheme("system");
        preference.setUpdatedAt(now);
        userPreferenceRepository.save(preference);

        if (!mailEnabled) {
            user.setEmailVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("User registered with mail disabled, userId={}, email={}. Returning auth tokens immediately.", user.getId(), user.getEmail());
            return new VerificationResponse(
                    "User registered successfully. Email verification skipped because mail is disabled",
                    user.getEmail(),
                    null,
                    buildAuthResponse(user)
            );
        }

        log.info("User registered successfully, userId={}, email={}. Verification required.", user.getId(), user.getEmail());
        return emailVerificationService.issueRegistrationCode(user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login request received for email={}", request.email());
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login rejected for email={}", request.email());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Email is not verified");
        }

        log.info("Login successful for userId={}, email={}", user.getId(), user.getEmail());
        return buildAuthResponse(user);
    }

    public VerificationResponse resendVerificationCode(EmailVerificationRequest request) {
        return emailVerificationService.issueRegistrationCode(request.email());
    }

    public void confirmEmail(EmailVerificationConfirmRequest request) {
        emailVerificationService.confirmRegistrationCode(request.email(), request.code());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        var currentRefreshToken = refreshTokenService.requireActive(request.refreshToken());
        User user = currentRefreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "User is not active");
        }
        var rotated = refreshTokenService.rotate(request.refreshToken());
        var accessToken = jwtService.generateAccessToken(user);
        log.info("Refresh successful for userId={}, email={}", user.getId(), user.getEmail());
        return new AuthResponse(
                accessToken.token(),
                rotated.token(),
                "Bearer",
                accessToken.expiresIn(),
                userService.toResponse(user)
        );
    }

    public void logout(AppUserPrincipal principal, RefreshTokenRequest request) {
        if (principal != null) {
            log.info("Logout for userId={}", principal.userId());
        } else {
            log.info("Logout by refresh token without authenticated principal");
        }
        refreshTokenService.revoke(request.refreshToken());
    }

    @Transactional
    public PasswordResetResponse requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        log.info("Password reset requested for email={}", normalizedEmail);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null) {
            return new PasswordResetResponse("If the account exists, a reset token has been issued", null);
        }

        String rawToken = UUID.randomUUID() + "." + UUID.randomUUID();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(token);
        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        return new PasswordResetResponse("Password reset instructions have been sent to email", null);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        validatePassword(request.newPassword());
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                        hashToken(request.token()), LocalDateTime.now())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired password reset token"));
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
        log.info("Password reset confirmed for userId={}, email={}", user.getId(), user.getEmail());
    }

    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must contain 8-64 chars, at least one letter and one digit");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = refreshTokenService.issue(user);
        return new AuthResponse(
                accessToken.token(),
                refreshToken.token(),
                "Bearer",
                accessToken.expiresIn(),
                userService.toResponse(user)
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
