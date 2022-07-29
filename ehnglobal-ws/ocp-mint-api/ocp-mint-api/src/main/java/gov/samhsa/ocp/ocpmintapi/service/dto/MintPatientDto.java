package gov.samhsa.ocp.ocpmintapi.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MintPatientDto {
    private List<MintOtherOrganizationDto> otherOrganizations;
    private String patientID;
    private MintDemographicsDto demographics;

    @Override
    public String toString() {
        return "MintPatientDto{" +
                "otherOrganizations=" + otherOrganizations +
                ", patientID='" + patientID + '\'' +
                ", demographics=" + demographics +
                '}';
    }
}

