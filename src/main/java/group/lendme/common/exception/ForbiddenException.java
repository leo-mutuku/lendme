package group.lendme.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 403: we know who you are, but you may not do this yet,
 * e.g. ACCOUNT_LOCKED, ACCOUNT_DISABLED, EMAIL_NOT_VERIFIED.
 */
public class ForbiddenException extends ApiException {

    private final String errorCode;

    public ForbiddenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
