package gov.samhsa.ocp.ocpmintapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MintPatientFlagsDto {
    private MintEntryDto entry;

    @Override
    public String toString() {
        return "PatientFlagsDto{" +
                "entry=" + entry +
                '}';
    }
}
