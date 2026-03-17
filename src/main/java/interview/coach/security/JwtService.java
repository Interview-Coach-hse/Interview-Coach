package interview.coach.security;

import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.access-token-minutes}") long accessTokenMinutes
    ) {
        if (secret.length() < 32) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "JWT secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public TokenPayload generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenMinutes * 60);
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getCode())
                .signWith(signingKey, SIG.HS256)
                .compact();
        return new TokenPayload(token, expiresAt.getEpochSecond() - now.getEpochSecond());
    }

    public JwtClaims parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new JwtClaims(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("role", String.class),
                    LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneOffset.UTC)
            );
        } catch (ExpiredJwtException exception) {
            throw new BadCredentialsException("Access token expired", exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("Invalid access token", exception);
        }
    }

    public record JwtClaims(
            UUID userId,
            String email,
            String roleCode,
            LocalDateTime expiresAt
    ) {
    }

    public record TokenPayload(
            String token,
            long expiresIn
    ) {
    }
}
