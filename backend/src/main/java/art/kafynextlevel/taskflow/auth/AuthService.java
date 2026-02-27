package art.kafynextlevel.taskflow.auth;

import java.util.Locale;

import art.kafynextlevel.taskflow.user.User;
import art.kafynextlevel.taskflow.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthTokens login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmail(normalizedEmail).orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return jwtService.issueTokens(user.getId(), user.getEmail());
    }

    public AuthTokens refresh(String refreshToken) {
        TokenPrincipal principal = jwtService.parseRefreshToken(refreshToken);
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));

        if (!user.getEmail().equalsIgnoreCase(principal.email())) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }

        return jwtService.issueTokens(user.getId(), user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
