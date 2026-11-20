package group.lendme.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 404: e.g. throw new ResourceNotFoundException("Loan", loanId);
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s with id %s was not found".formatted(resource, id));
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super("%s with %s '%s' was not found".formatted(resource, field, value));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}