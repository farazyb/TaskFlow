package art.kafynextlevel.taskflow.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import art.kafynextlevel.taskflow.user.User;
import art.kafynextlevel.taskflow.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_issuesTokens_whenCredentialsAreValid() {
        User user = org.mockito.Mockito.mock(User.class);
        UUID userId = UUID.randomUUID();
        AuthTokens tokens = new AuthTokens("access", "refresh", Instant.now(), Instant.now().plusSeconds(3600));

        when(userRepository.findByEmail("faraz@example.com")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("faraz@example.com");
        when(user.getPasswordHash()).thenReturn("stored-hash");
        when(passwordEncoder.matches("P@ssw0rd-123", "stored-hash")).thenReturn(true);
        when(jwtService.issueTokens(userId, "faraz@example.com")).thenReturn(tokens);

        AuthTokens result = authService.login(new LoginRequest(" Faraz@Example.com ", "P@ssw0rd-123"));

        assertThat(result).isEqualTo(tokens);
        verify(userRepository).findByEmail("faraz@example.com");
    }

    @Test
    void login_throwsUnauthorized_whenEmailDoesNotExist() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "P@ssw0rd-123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsUnauthorized_whenPasswordDoesNotMatch() {
        User user = org.mockito.Mockito.mock(User.class);
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.of(user));
        when(user.getPasswordHash()).thenReturn("stored-hash");
        when(passwordEncoder.matches("wrong-pass", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrong@example.com", "wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_issuesNewTokens_whenRefreshTokenMatchesUser() {
        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        AuthTokens rotated = new AuthTokens("new-access", "new-refresh", Instant.now(), Instant.now().plusSeconds(100));

        when(jwtService.parseRefreshToken("valid-refresh-token"))
                .thenReturn(new TokenPrincipal(userId, "refresh@example.com"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("refresh@example.com");
        when(jwtService.issueTokens(userId, "refresh@example.com")).thenReturn(rotated);

        AuthTokens result = authService.refresh("valid-refresh-token");

        assertThat(result).isEqualTo(rotated);
    }

    @Test
    void refresh_throwsUnauthorized_whenUserNoLongerExists() {
        UUID userId = UUID.randomUUID();
        when(jwtService.parseRefreshToken("refresh-token"))
                .thenReturn(new TokenPrincipal(userId, "missing@example.com"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void refresh_throwsUnauthorized_whenTokenEmailDoesNotMatchUser() {
        UUID userId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        when(jwtService.parseRefreshToken("refresh-token"))
                .thenReturn(new TokenPrincipal(userId, "token@example.com"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("db@example.com");

        assertThatThrownBy(() -> authService.refresh("refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("invalid");
    }
}
