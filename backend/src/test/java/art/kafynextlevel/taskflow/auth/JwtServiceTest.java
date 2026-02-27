package art.kafynextlevel.taskflow.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "taskflow-jwt-secret-for-tests-32-bytes";

    private JwtAuthProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtAuthProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
    }

    @Test
    void issueTokens_createsHs256TokensWithExpectedClaims() {
        Instant now = Instant.parse("2026-02-27T12:00:00Z");
        JwtService jwtService = new JwtService(properties, Clock.fixed(now, ZoneOffset.UTC));

        UUID userId = UUID.randomUUID();
        AuthTokens tokens = jwtService.issueTokens(userId, "user@example.com");

        Claims accessClaims = parseClaims(tokens.accessToken(), SECRET, now.plusSeconds(1));
        Claims refreshClaims = parseClaims(tokens.refreshToken(), SECRET, now.plusSeconds(1));

        assertThat(accessClaims.getSubject()).isEqualTo(userId.toString());
        assertThat(accessClaims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(accessClaims.get("type", String.class)).isEqualTo("access");

        assertThat(refreshClaims.getSubject()).isEqualTo(userId.toString());
        assertThat(refreshClaims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(refreshClaims.get("type", String.class)).isEqualTo("refresh");

        assertThat(tokens.accessTokenExpiresAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
        assertThat(tokens.refreshTokenExpiresAt()).isEqualTo(now.plus(Duration.ofDays(7)));
    }

    @Test
    void parseRefreshToken_returnsTokenPrincipal_whenRefreshTokenIsValid() {
        JwtService jwtService = new JwtService(properties, Clock.systemUTC());
        UUID userId = UUID.randomUUID();
        AuthTokens tokens = jwtService.issueTokens(userId, "refresh@example.com");

        TokenPrincipal principal = jwtService.parseRefreshToken(tokens.refreshToken());

        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.email()).isEqualTo("refresh@example.com");
    }

    @Test
    void parseAccessToken_returnsTokenPrincipal_whenAccessTokenIsValid() {
        JwtService jwtService = new JwtService(properties, Clock.systemUTC());
        UUID userId = UUID.randomUUID();
        AuthTokens tokens = jwtService.issueTokens(userId, "access@example.com");

        TokenPrincipal principal = jwtService.parseAccessToken(tokens.accessToken());

        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.email()).isEqualTo("access@example.com");
    }

    @Test
    void parseAccessToken_rejectsRefreshToken() {
        JwtService jwtService = new JwtService(properties, Clock.systemUTC());
        AuthTokens tokens = jwtService.issueTokens(UUID.randomUUID(), "refresh-only@example.com");

        assertThatThrownBy(() -> jwtService.parseAccessToken(tokens.refreshToken()))
                .isInstanceOf(InvalidAccessTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void parseAccessToken_rejectsExpiredToken() {
        Instant issueAt = Instant.parse("2026-02-20T10:00:00Z");
        JwtService signer = new JwtService(properties, Clock.fixed(issueAt, ZoneOffset.UTC));
        AuthTokens tokens = signer.issueTokens(UUID.randomUUID(), "expired-access@example.com");

        JwtService parser = new JwtService(
                properties,
                Clock.fixed(issueAt.plus(Duration.ofMinutes(16)), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> parser.parseAccessToken(tokens.accessToken()))
                .isInstanceOf(InvalidAccessTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void parseRefreshToken_rejectsAccessToken() {
        JwtService jwtService = new JwtService(properties, Clock.systemUTC());
        AuthTokens tokens = jwtService.issueTokens(UUID.randomUUID(), "access@example.com");

        assertThatThrownBy(() -> jwtService.parseRefreshToken(tokens.accessToken()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void parseRefreshToken_rejectsExpiredToken() {
        Instant issueAt = Instant.parse("2026-02-20T10:00:00Z");
        JwtService signer = new JwtService(properties, Clock.fixed(issueAt, ZoneOffset.UTC));
        AuthTokens tokens = signer.issueTokens(UUID.randomUUID(), "expired@example.com");

        JwtService parser = new JwtService(
                properties,
                Clock.fixed(issueAt.plus(Duration.ofDays(8)), ZoneOffset.UTC)
        );

        assertThatThrownBy(() -> parser.parseRefreshToken(tokens.refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void parseRefreshToken_rejectsTokenSignedWithDifferentSecret() {
        JwtAuthProperties anotherProps = new JwtAuthProperties();
        anotherProps.setSecret("another-jwt-secret-for-tests-32-bytes");
        anotherProps.setAccessTokenTtl(Duration.ofMinutes(10));
        anotherProps.setRefreshTokenTtl(Duration.ofDays(7));

        JwtService anotherSigner = new JwtService(anotherProps, Clock.systemUTC());
        AuthTokens tokens = anotherSigner.issueTokens(UUID.randomUUID(), "wrong-signature@example.com");

        JwtService jwtService = new JwtService(properties, Clock.systemUTC());
        assertThatThrownBy(() -> jwtService.parseRefreshToken(tokens.refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("invalid");
    }

    private Claims parseClaims(String token, String secret, Instant clockInstant) {
        return Jwts.parser()
                .clock(() -> java.util.Date.from(clockInstant))
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
