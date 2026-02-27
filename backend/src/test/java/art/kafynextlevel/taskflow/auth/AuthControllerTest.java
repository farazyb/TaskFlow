package art.kafynextlevel.taskflow.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private JwtAuthProperties properties;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        properties = new JwtAuthProperties();
        properties.setSecret("taskflow-jwt-secret-for-tests-32-bytes");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        properties.setRefreshCookieName("refresh_token");
        properties.setRefreshCookiePath("/api/v1/auth/refresh");
        properties.setRefreshCookieSameSite("Strict");
        properties.setRefreshCookieSecure(false);

        authController = new AuthController(authService, properties);
    }

    @Test
    void login_returnsAccessToken_andSetsRefreshCookie() {
        AuthTokens tokens = new AuthTokens(
                "access-token",
                "refresh-token",
                Instant.now().plus(Duration.ofMinutes(15)),
                Instant.now().plus(Duration.ofDays(7))
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(tokens);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity<AuthTokenResponse> result = authController.login(
                new LoginRequest("user@example.com", "P@ssw0rd-123"),
                response
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().accessToken()).isEqualTo("access-token");
        assertThat(result.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(result.getBody().expiresInSeconds()).isEqualTo(900);

        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("refresh_token=refresh-token");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/api/v1/auth/refresh");
        assertThat(setCookie).contains("SameSite=Strict");
    }

    @Test
    void refresh_readsCookie_returnsAccessToken_andSetsNewRefreshCookie() {
        AuthTokens tokens = new AuthTokens(
                "new-access-token",
                "new-refresh-token",
                Instant.now().plus(Duration.ofMinutes(15)),
                Instant.now().plus(Duration.ofDays(7))
        );
        when(authService.refresh("existing-refresh-token")).thenReturn(tokens);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh_token", "existing-refresh-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<AuthTokenResponse> result = authController.refresh(request, response);

        verify(authService).refresh("existing-refresh-token");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().accessToken()).isEqualTo("new-access-token");

        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("refresh_token=new-refresh-token");
    }

    @Test
    void refresh_throws_whenCookiesAreMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authController.refresh(request, response))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void refresh_throws_whenRefreshCookieIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("session_id", "abc"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> authController.refresh(request, response))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("missing");
    }
}
