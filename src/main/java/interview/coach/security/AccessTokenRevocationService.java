package interview.coach.security;

import interview.coach.domain.entity.RevokedAccessToken;
import interview.coach.repository.RevokedAccessTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessTokenRevocationService {

    private final RevokedAccessTokenRepository revokedAccessTokenRepository;

    public AccessTokenRevocationService(RevokedAccessTokenRepository revokedAccessTokenRepository) {
        this.revokedAccessTokenRepository = revokedAccessTokenRepository;
    }

    @Transactional
    public void revoke(String rawAccessToken, LocalDateTime expiresAt) {
        LocalDateTime now = nowUtc();
        if (rawAccessToken == null || rawAccessToken.isBlank() || expiresAt == null || !expiresAt.isAfter(now)) {
            return;
        }

        String tokenHash = hashToken(rawAccessToken);
        if (revokedAccessTokenRepository.existsByTokenHashAndExpiresAtAfter(tokenHash, now)) {
            return;
        }

        RevokedAccessToken revokedAccessToken = new RevokedAccessToken();
        revokedAccessToken.setTokenHash(tokenHash);
        revokedAccessToken.setExpiresAt(expiresAt);
        revokedAccessToken.setRevokedAt(now);
        revokedAccessTokenRepository.save(revokedAccessToken);
    }

    public boolean isRevoked(String rawAccessToken) {
        if (rawAccessToken == null || rawAccessToken.isBlank()) {
            return false;
        }
        return revokedAccessTokenRepository.existsByTokenHashAndExpiresAtAfter(hashToken(rawAccessToken), nowUtc());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
