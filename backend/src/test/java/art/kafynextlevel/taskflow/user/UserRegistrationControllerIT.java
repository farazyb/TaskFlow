package art.kafynextlevel.taskflow.user;

import static org.assertj.core.api.Assertions.assertThat;

import art.kafynextlevel.taskflow.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest
@AutoConfigureRestTestClient
@Import(TestcontainersConfig.class)
class UserRegistrationControllerIT {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_returns201_andStoresHashedPassword() {
        String rawPassword = "P@ssw0rd-123";
        String requestBody = """
                {
                  "email": "new.user@example.com",
                  "password": "%s",
                  "fullName": "New User"
                }
                """.formatted(rawPassword);

        RestTestClient.ResponseSpec response = restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange();

        response.expectStatus().isCreated();
        response.expectBody()
                .jsonPath("$.email").isEqualTo("new.user@example.com")
                .jsonPath("$.fullName").isEqualTo("New User");

        User saved = userRepository.findByEmail("new.user@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, saved.getPasswordHash())).isTrue();
    }

    @Test
    void register_returns400_whenPayloadInvalid() {
        String invalidBody = """
                {
                  "email": "not-an-email",
                  "password": "123",
                  "fullName": ""
                }
                """;

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_FAILED")
                .jsonPath("$.fieldErrors.email").exists()
                .jsonPath("$.fieldErrors.password").exists()
                .jsonPath("$.fieldErrors.fullName").exists();
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() {
        userRepository.save(new User(
                "existing@example.com",
                passwordEncoder.encode("P@ssw0rd-123"),
                "Existing User"
        ));

        String duplicateBody = """
                {
                  "email": "EXISTING@example.com",
                  "password": "P@ssw0rd-123",
                  "fullName": "Another User"
                }
                """;

        restTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(duplicateBody)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.error").isEqualTo("EMAIL_ALREADY_EXISTS");
    }
}
