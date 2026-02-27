package art.kafynextlevel.taskflow.auth;

import art.kafynextlevel.taskflow.TestcontainersConfig;
import art.kafynextlevel.taskflow.user.User;
import art.kafynextlevel.taskflow.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest
@AutoConfigureRestTestClient
@Import(TestcontainersConfig.class)
class AuthControllerIT {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void login_returns200_withAccessToken_andRefreshCookie() {
        userRepository.save(new User(
                "login.user@example.com",
                passwordEncoder.encode("P@ssw0rd-123"),
                "Login User"
        ));

        String requestBody = """
                {
                  "email": "login.user@example.com",
                  "password": "P@ssw0rd-123"
                }
                """;

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(
                        HttpHeaders.SET_COOKIE,
                        "refresh_token=.*Path=/api/v1/auth/refresh.*HttpOnly.*SameSite=Strict.*"
                )
                .expectBody()
                .jsonPath("$.accessToken").exists()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.expiresInSeconds").isEqualTo(900);
    }

    @Test
    void login_returns401_whenCredentialsAreInvalid() {
        userRepository.save(new User(
                "login.fail@example.com",
                passwordEncoder.encode("P@ssw0rd-123"),
                "Login Fail"
        ));

        String requestBody = """
                {
                  "email": "login.fail@example.com",
                  "password": "wrong-password"
                }
                """;

        restTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void refresh_returns200_andRotatesRefreshCookie_whenCookieIsValid() {
        User saved = userRepository.save(new User(
                "refresh.user@example.com",
                passwordEncoder.encode("P@ssw0rd-123"),
                "Refresh User"
        ));
        String refreshToken = jwtService.issueTokens(saved.getId(), saved.getEmail()).refreshToken();

        restTestClient.post()
                .uri("/api/v1/auth/refresh")
                .header(HttpHeaders.COOKIE, "refresh_token=" + refreshToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(
                        HttpHeaders.SET_COOKIE,
                        "refresh_token=.*Path=/api/v1/auth/refresh.*HttpOnly.*SameSite=Strict.*"
                )
                .expectBody()
                .jsonPath("$.accessToken").exists()
                .jsonPath("$.tokenType").isEqualTo("Bearer");
    }

    @Test
    void refresh_returns401_whenCookieMissing() {
        restTestClient.post()
                .uri("/api/v1/auth/refresh")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INVALID_REFRESH_TOKEN");
    }

    @Test
    void refresh_returns401_whenTokenInvalid() {
        restTestClient.post()
                .uri("/api/v1/auth/refresh")
                .header(HttpHeaders.COOKIE, "refresh_token=not-a-valid-jwt")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INVALID_REFRESH_TOKEN");
    }
}
