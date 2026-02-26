package art.kafynextlevel.taskflow.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void registerUser_hashesPassword_andNormalizesInput() {
        RegisterUserRequest request = new RegisterUserRequest(
                " Faraz@Example.com ",
                "P@ssw0rd-123",
                " Faraz Yazdani "
        );

        when(userRepository.existsByEmailIgnoreCase("faraz@example.com")).thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd-123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserResponse response = userRegistrationService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("faraz@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(savedUser.getFullName()).isEqualTo("Faraz Yazdani");

        assertThat(response.email()).isEqualTo("faraz@example.com");
        assertThat(response.fullName()).isEqualTo("Faraz Yazdani");
    }

    @Test
    void registerUser_throwsConflict_whenEmailExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "existing@example.com",
                "P@ssw0rd-123",
                "Faraz Yazdani"
        );
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userRegistrationService.registerUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }
}
