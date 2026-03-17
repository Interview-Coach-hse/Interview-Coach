package interview.coach.security;

import interview.coach.domain.entity.RefreshToken;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.security.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public RefreshTokenPayload issue(User user) {
        String rawToken = UUID.randomUUID() + "." + UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenDays));
        refreshTokenRepository.save(refreshToken);
        return new RefreshTokenPayload(rawToken, refreshToken.getExpiresAt());
    }

    public RefreshToken requireActive(String rawRefreshToken) {
        return refreshTokenRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(hashToken(rawRefreshToken), LocalDateTime.now())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hashToken(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public RefreshTokenPayload rotate(String rawRefreshToken) {
        RefreshToken current = requireActive(rawRefreshToken);
        current.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(current);
        return issue(current.getUser());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public record RefreshTokenPayload(
            String token,
            LocalDateTime expiresAt
    ) {
    }
}
