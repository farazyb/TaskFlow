package art.kafynextlevel.taskflow.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderConfigTest {
    @Test
    void passwordEncoderBean_isBCrypt_withStrength12() {
        SecurityConfig securityConfig = new SecurityConfig();
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);

        String hash = encoder.encode("P@ssw0rd-123");
        assertThat(hash).matches("^\\$2[aby]\\$12\\$.*");
        assertThat(encoder.matches("P@ssw0rd-123", hash)).isTrue();
    }
}
