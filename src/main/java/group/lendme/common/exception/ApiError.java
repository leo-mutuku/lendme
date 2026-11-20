package group.lendme.common.exception;


import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

/**
 * The JSON body returned for every error, so the frontend always gets the same shape:
 *
 * {
 *   "timestamp": "...", "status": 404, "error": "Not Found",
 *   "code": "NOT_FOUND", "message": "Loan with id 5 was not found",
 *   "path": "/api/loans/5", "fieldErrors": []
 * }
 *
 * A record is immutable (encapsulation): once built, nothing can change it.
 * Static factory methods hide how it is constructed.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldViolation> fieldErrors
) {

    public record FieldViolation(String field, String message) {
    }

    public static ApiError of(HttpStatus status, String code, String message, String path) {
        return of(status, code, message, path, List.of());
    }

    public static ApiError of(HttpStatus status, String code, String message, String path,
                              List<FieldViolation> fieldErrors) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                List.copyOf(fieldErrors)
        );
    }
}