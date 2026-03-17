package interview.coach.service;

import interview.coach.api.dto.AuthDtos.VerificationResponse;
import interview.coach.domain.DomainEnums.VerificationPurpose;
import interview.coach.domain.entity.EmailVerificationCode;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.EmailVerificationCodeRepository;
import interview.coach.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final boolean mailEnabled;
    private final long expirationMinutes;
    private final int maxAttempts;
    private final Random random = new Random();

    public EmailVerificationService(
            EmailVerificationCodeRepository emailVerificationCodeRepository,
            UserRepository userRepository,
            EmailService emailService,
            @Value("${app.mail.enabled}") boolean mailEnabled,
            @Value("${app.security.email-verification.expiration-minutes}") long expirationMinutes,
            @Value("${app.security.email-verification.max-attempts}") int maxAttempts
    ) {
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.mailEnabled = mailEnabled;
        this.expirationMinutes = expirationMinutes;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public VerificationResponse issueRegistrationCode(String email) {
        String normalizedEmail = normalize(email);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.isEmailVerified()) {
            return new VerificationResponse("Email is already verified", normalizedEmail, null);
        }

        invalidateActive(normalizedEmail, VerificationPurpose.REGISTRATION);

        String rawCode = generateCode();
        EmailVerificationCode code = new EmailVerificationCode();
        code.setEmail(normalizedEmail);
        code.setCodeHash(hash(rawCode));
        code.setPurpose(VerificationPurpose.REGISTRATION);
        code.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        code.setAttempts(0);
        code.setUsed(false);
        code.setCreatedAt(LocalDateTime.now());
        emailVerificationCodeRepository.save(code);

        emailService.sendEmailVerificationCode(normalizedEmail, rawCode);
        return new VerificationResponse(
                "Verification code has been sent",
                normalizedEmail,
                mailEnabled ? null : rawCode
        );
    }

    @Transactional
    public void confirmRegistrationCode(String email, String rawCode) {
        String normalizedEmail = normalize(email);
        EmailVerificationCode code = emailVerificationCodeRepository
                .findTopByEmailIgnoreCaseAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        normalizedEmail,
                        VerificationPurpose.REGISTRATION,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Verification code is invalid or expired"));

        if (code.getAttempts() >= maxAttempts) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Verification code attempts exceeded");
        }

        if (!code.getCodeHash().equals(hash(rawCode))) {
            code.setAttempts(code.getAttempts() + 1);
            emailVerificationCodeRepository.save(code);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Verification code is invalid");
        }

        code.setUsed(true);
        code.setAttempts(code.getAttempts() + 1);
        emailVerificationCodeRepository.save(code);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void invalidateActive(String email, VerificationPurpose purpose) {
        emailVerificationCodeRepository.findByEmailIgnoreCaseAndPurposeAndUsedFalse(email, purpose)
                .forEach(code -> {
                    code.setUsed(true);
                    emailVerificationCodeRepository.save(code);
                });
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateCode() {
        return "%06d".formatted(random.nextInt(1_000_000));
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
