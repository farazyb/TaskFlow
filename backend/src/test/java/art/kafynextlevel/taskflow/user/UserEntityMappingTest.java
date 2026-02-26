package art.kafynextlevel.taskflow.user;

import art.kafynextlevel.taskflow.TestcontainersConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.*;

@Import(TestcontainersConfig.class)
@DataJpaTest
public class UserEntityMappingTest {
    @Autowired
    EntityManager em;


    @Test
    void persistUser_roundTrip() {
        User user = new User(
                " Faraz@Example.com  ",  // intentionally messy
                "$2a$12$hash...",
                "Faraz YazdaniBiuki"
        );
        em.persist(user);
        em.flush();
        em.clear();

        var reloaded = em.find(User.class, user.getId());
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getEmail()).isEqualTo("faraz@example.com"); // normalized
        assertThat(reloaded.getPasswordHash()).isEqualTo("$2a$12$hash...");
        assertThat(reloaded.getFullName()).isEqualTo("Faraz YazdaniBiuki");
        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    @Test
    void email_isUnique() {
        em.persist(new User("Test@example.com", "$2a$12$hash...", "Faraz YazdaniBiuki"));
        em.flush();
        em.persist(new User("TEST@EXAMPLE.COM", "$2a$12$hash", "Akbar"));
        assertThatThrownBy(() -> em.flush()).isInstanceOf(ConstraintViolationException.class);
    }
}
