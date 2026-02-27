package art.kafynextlevel.taskflow.auth;

import java.util.UUID;

record TokenPrincipal(
        UUID userId,
        String email
) {
}
