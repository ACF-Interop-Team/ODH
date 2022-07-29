package gov.samhsa.ocp.ocpmintapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {
    private String message;
    private HttpStatus status;
    private ErrorCode errorCode;

    public class ErrorCode {
        private String numCode;
        private String txtCode;

        /** getters **/
    }
}

