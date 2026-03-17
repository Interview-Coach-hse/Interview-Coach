package interview.coach.service;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String mailFrom;
    private final String passwordResetSubject;
    private final String emailVerificationSubject;
    private final String passwordResetUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled}") boolean mailEnabled,
            @Value("${app.mail.from}") String mailFrom,
            @Value("${app.mail.password-reset-subject}") String passwordResetSubject,
            @Value("${app.mail.email-verification-subject}") String emailVerificationSubject,
            @Value("${app.frontend.password-reset-url}") String passwordResetUrl
    ) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.mailFrom = mailFrom;
        this.passwordResetSubject = passwordResetSubject;
        this.emailVerificationSubject = emailVerificationSubject;
        this.passwordResetUrl = passwordResetUrl;
    }

    public void sendPasswordResetEmail(String recipient, String token) {
        String resetLink = passwordResetUrl + "?token=" + token;
        String body = """
                You requested a password reset for Interview Coach.

                Use this link to set a new password:
                %s

                If you did not request this change, ignore this email.
                """.formatted(resetLink);

        if (!mailEnabled) {
            log.warn("Mail sending is disabled. Password reset link for {}: {}", recipient, resetLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipient);
        message.setSubject(passwordResetSubject);
        message.setText(body);
        mailSender.send(message);
        log.info("Password reset email sent to {}", recipient);
    }

    public void sendEmailVerificationCode(String recipient, String code) {
        String body = """
                Your Interview Coach verification code is:

                %s

                This code expires shortly. If you did not create an account, ignore this email.
                """.formatted(code);

        if (!mailEnabled) {
            log.warn("Mail sending is disabled. Verification code for {}: {}", recipient, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipient);
        message.setSubject(emailVerificationSubject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email verification code sent to {}", recipient);
    }
}
