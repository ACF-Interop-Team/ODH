package gov.samhsa.ocp.ocpmintapi.service.exception;

import gov.samhsa.ocp.ocpmintapi.service.dto.ResponseDTO;
import org.springframework.http.HttpStatus;

public class CustomRuntimeException extends RuntimeException {
    public CustomRuntimeException() {
    }

    public CustomRuntimeException(String message) {
        super(message);
    }

    public CustomRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomRuntimeException(Throwable cause) {
        super(cause);
    }

    public CustomRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CustomRuntimeException(String message, HttpStatus cause, ResponseDTO.ErrorCode errorCode) {
        super(message + cause.toString() + errorCode.toString());
    }

    public CustomRuntimeException(String message, HttpStatus cause) {
        super(message + cause.toString());
    }

    public CustomRuntimeException(HttpStatus cause) {
        super(cause.toString());
    }
}