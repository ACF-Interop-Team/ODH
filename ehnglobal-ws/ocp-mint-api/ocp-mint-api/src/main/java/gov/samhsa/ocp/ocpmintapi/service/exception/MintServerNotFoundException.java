package gov.samhsa.ocp.ocpmintapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Mint Server Connection Refused")
public class MintServerNotFoundException extends RuntimeException {
    public MintServerNotFoundException() {
        super();
    }

    public MintServerNotFoundException(String message, Throwable cause,
                                       boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MintServerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MintServerNotFoundException(String message) {
        super(message);
    }

    public MintServerNotFoundException(Throwable cause) {
        super(cause);
    }
}
