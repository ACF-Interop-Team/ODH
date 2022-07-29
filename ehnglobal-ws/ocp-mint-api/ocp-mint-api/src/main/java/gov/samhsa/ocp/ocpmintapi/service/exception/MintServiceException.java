package gov.samhsa.ocp.ocpmintapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MintServiceException extends RuntimeException {
    private String error;
    private String message;

    public MintServiceException() {
    }

    public MintServiceException(String message) {
        super(message);
    }

    public MintServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MintServiceException(Throwable cause) {
        super(cause);
    }

    public MintServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MintServiceException(HttpStatus statusCode) {
    }
}
