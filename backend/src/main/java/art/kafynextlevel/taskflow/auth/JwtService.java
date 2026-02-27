package art.kafynextlevel.taskflow.auth;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtAuthProperties properties;
    private final SecretKey signingKey;
    private final Clock clock;

    @Autowired
    public JwtService(JwtAuthProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtService(JwtAuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public AuthTokens issueTokens(UUID userId, String email) {
        Instant now = Instant.now(clock);
        Instant accessExpiresAt = now.plus(properties.getAccessTokenTtl());
        Instant refreshExpiresAt = now.plus(properties.getRefreshTokenTtl());

        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExpiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new AuthTokens(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
    }

    public TokenPrincipal parseRefreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .clock(() -> Date.from(Instant.now(clock)))
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired", exception);
        }

        String tokenType = claims.get(CLAIM_TYPE, String.class);
        if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        String subject = claims.getSubject();
        String email = claims.get(CLAIM_EMAIL, String.class);
        if (subject == null || email == null || email.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        try {
            return new TokenPrincipal(UUID.fromString(subject), email);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException("Refresh token is invalid", exception);
        }
    }
}
