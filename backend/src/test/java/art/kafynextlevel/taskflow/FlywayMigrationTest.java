package art.kafynextlevel.taskflow;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void allMigrationsApplySuccessfully() {
        var info = flyway.info();
        assertEquals(3, info.applied().length,
                "Expected 3 Flyway migrations to be applied");
    }
}
