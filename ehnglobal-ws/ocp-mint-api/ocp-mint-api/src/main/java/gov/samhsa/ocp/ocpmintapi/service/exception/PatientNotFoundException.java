package gov.samhsa.ocp.ocpmintapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Patient(s) Not Found")
public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException() {
        super();
    }

    public PatientNotFoundException(String message, Throwable cause,
                                    boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PatientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatientNotFoundException(String message) {
        super(message);
    }

    public PatientNotFoundException(Throwable cause) {
        super(cause);
    }
}
