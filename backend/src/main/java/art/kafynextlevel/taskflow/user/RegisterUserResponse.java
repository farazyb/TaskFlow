package art.kafynextlevel.taskflow.user;

import java.time.Instant;
import java.util.UUID;

public record RegisterUserResponse(
        UUID id,
        String email,
        String fullName,
        Instant createdAt
) {
}
