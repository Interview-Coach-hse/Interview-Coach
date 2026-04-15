package interview.coach.repository;

import interview.coach.domain.entity.RevokedAccessToken;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, UUID> {

    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);
}
