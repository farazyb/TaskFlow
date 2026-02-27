package art.kafynextlevel.taskflow.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtAuthProperties properties;

    public AuthController(AuthService authService, JwtAuthProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthTokens tokens = authService.login(request);
        attachRefreshTokenCookie(response, tokens.refreshToken());

        AuthTokenResponse payload = new AuthTokenResponse(
                tokens.accessToken(),
                "Bearer",
                properties.getAccessTokenTtl().toSeconds()
        );
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(request);
        AuthTokens tokens = authService.refresh(refreshToken);
        attachRefreshTokenCookie(response, tokens.refreshToken());

        AuthTokenResponse payload = new AuthTokenResponse(
                tokens.accessToken(),
                "Bearer",
                properties.getAccessTokenTtl().toSeconds()
        );
        return ResponseEntity.ok(payload);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidRefreshTokenException("Refresh token is missing");
        }

        for (Cookie cookie : cookies) {
            if (properties.getRefreshCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new InvalidRefreshTokenException("Refresh token is missing");
    }

    private void attachRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(properties.getRefreshCookieName(), refreshToken)
                .httpOnly(true)
                .secure(properties.isRefreshCookieSecure())
                .sameSite(properties.getRefreshCookieSameSite())
                .path(properties.getRefreshCookiePath())
                .maxAge(properties.getRefreshTokenTtl())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
