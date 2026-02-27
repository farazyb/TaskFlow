package art.kafynextlevel.taskflow.auth;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
