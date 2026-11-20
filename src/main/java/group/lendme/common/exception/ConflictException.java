package group.lendme.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 409: the data already exists, e.g. "Email already registered".
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}