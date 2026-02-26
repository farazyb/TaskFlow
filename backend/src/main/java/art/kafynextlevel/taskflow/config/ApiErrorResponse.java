package art.kafynextlevel.taskflow.config;

import java.util.Map;

public record ApiErrorResponse(
        String error,
        String message,
        Map<String, String> fieldErrors
) {
}
