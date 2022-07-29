package gov.samhsa.ocp.ocpmintapi.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MintPatientRequestDto {
    private String patientID;
    private MintDemographicsDto demographics;

    @Override
    public String toString() {
        return "MintPatientRequestDto{" +
                "patientID='" + patientID + '\'' +
                ", demographics=" + demographics +
                '}';
    }
}

