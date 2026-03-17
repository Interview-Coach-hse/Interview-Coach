package interview.coach.repository;

import interview.coach.domain.DomainEnums.VerificationPurpose;
import interview.coach.domain.entity.EmailVerificationCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, UUID> {

    Optional<EmailVerificationCode> findTopByEmailIgnoreCaseAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email,
            VerificationPurpose purpose,
            LocalDateTime now
    );

    List<EmailVerificationCode> findByEmailIgnoreCaseAndPurposeAndUsedFalse(String email, VerificationPurpose purpose);
}
