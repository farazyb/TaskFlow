package art.kafynextlevel.taskflow.auth;

import java.time.Instant;

record AuthTokens(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
