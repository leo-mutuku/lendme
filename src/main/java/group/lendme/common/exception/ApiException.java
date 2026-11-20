package group.lendme.common.exception;


import org.springframework.http.HttpStatus;

/**
 * Base class for every error LendMe throws on purpose.
 *
 * OOP:
 *  - Abstraction:  each subclass must say which HTTP status it maps to.
 *  - Polymorphism: GlobalExceptionHandler has ONE handler for ApiException and
 *                  calls getStatus()/getErrorCode(); each subclass answers differently.
 *                  Adding a new exception type needs no change to the handler.
 */
public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();

    /** Machine-readable code for the frontend, e.g. NOT_FOUND. Subclasses can override. */
    public String getErrorCode() {
        return getStatus().name();
    }
}
