package art.kafynextlevel.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithTests {

    @Test
    void verifiesModules() {
        ApplicationModules modules = ApplicationModules.of(TaskflowApplication.class);
        modules.forEach(System.out::println);
        modules.verify();
    }

    @Test
    void createDocument() {
        ApplicationModules modules = ApplicationModules.of(TaskflowApplication.class);
        new Documenter(modules).writeDocumentation();
    }
}
