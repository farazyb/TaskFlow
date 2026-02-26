package art.kafynextlevel.taskflow.user;


import static org.assertj.core.api.Assertions.*;

import art.kafynextlevel.taskflow.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfig.class)
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    public void findByEmail_returnsUser_whenExists() {
        // arrange
        var saved = userRepository.save(
                new User("  Faraz@Example.com ", "hash", "Faraz YazdaniBiuki")
        );

        // act
        var found = userRepository.findByEmail("faraz@example.com");

        // assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo("faraz@example.com");
    }

    @Test
    public void existsByEmail_returnsTrue_whenExists() {
        userRepository.save(new User(
                "  Faraz@Example.com  ", "hash", "Faraz YazdaniBiuki"
        ));
        var doesExist = userRepository.existsByEmailIgnoreCase("Faraz@Example.com");
        assertThat(doesExist).isTrue();
    }

}
