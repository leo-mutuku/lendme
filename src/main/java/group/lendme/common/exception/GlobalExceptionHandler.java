package group.lendme.common.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Turns exceptions from any controller into a consistent ApiError response.
 * Controllers and services just throw; they never build error responses themselves.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** One method covers every ApiException subclass (polymorphism). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        return respond(ApiError.of(ex.getStatus(), ex.getErrorCode(), ex.getMessage(), request.getRequestURI()));
    }

    /** @Valid failures on request DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldViolation(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return respond(ApiError.of(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Request validation failed", request.getRequestURI(), violations));
    }

    /** Unknown URL: a plain 404 instead of falling through to the 500 handler. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return respond(ApiError.of(HttpStatus.NOT_FOUND, "NOT_FOUND", "No endpoint at this path", request.getRequestURI()));
    }

    /** Value objects (Email, PhoneNumber...) reject bad input with IllegalArgumentException. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return respond(ApiError.of(HttpStatus.BAD_REQUEST, "INVALID_INPUT", ex.getMessage(), request.getRequestURI()));
    }

    /** Two updates hit the same record at once (see @Version in BaseEntity). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                         HttpServletRequest request) {
        return respond(ApiError.of(HttpStatus.CONFLICT, "CONCURRENT_UPDATE",
                "This record was changed by another request. Reload and try again.", request.getRequestURI()));
    }

    /** Anything unexpected: log the details, but don't leak them to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {}", request.getRequestURI(), ex);
        return respond(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Something went wrong. Please try again later.", request.getRequestURI()));
    }

    private ResponseEntity<ApiError> respond(ApiError body) {
        return ResponseEntity.status(body.status()).body(body);
    }
}
