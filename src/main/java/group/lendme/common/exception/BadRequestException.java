package group.lendme.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 400: the request itself is wrong (bad parameter, invalid date range...).
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
