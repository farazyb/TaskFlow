package art.kafynextlevel.taskflow.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import art.kafynextlevel.taskflow.auth.InvalidCredentialsException;
import art.kafynextlevel.taskflow.auth.InvalidRefreshTokenException;
import art.kafynextlevel.taskflow.user.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        ApiErrorResponse response = new ApiErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(EmailAlreadyExistsException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                "INVALID_REFRESH_TOKEN",
                exception.getMessage(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
