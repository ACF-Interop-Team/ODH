package gov.samhsa.ocp.ocpmintapi.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MintRootDto {
    private List<MintPatientResponseDto> patients;
    private String warning;

    @Override
    public String toString() {
        return "RootDto{" +
                "patients=" + patients +
                '}';
    }
}