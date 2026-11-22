package group.lendme.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 401: who you are could not be confirmed, e.g. wrong email/password.
 */
public class UnauthorizedException extends ApiException {

    private final String errorCode;

    public UnauthorizedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
