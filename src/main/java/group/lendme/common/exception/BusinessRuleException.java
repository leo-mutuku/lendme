package group.lendme.common.exception;


import org.springframework.http.HttpStatus;

/**
 * 422: the request is valid, but a lending rule says no.
 * e.g. new BusinessRuleException("LOAN_LIMIT_EXCEEDED", "Amount exceeds your loan limit");
 *
 * Overrides getErrorCode() so the frontend gets a specific code, not just a status.
 */
public class BusinessRuleException extends ApiException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
