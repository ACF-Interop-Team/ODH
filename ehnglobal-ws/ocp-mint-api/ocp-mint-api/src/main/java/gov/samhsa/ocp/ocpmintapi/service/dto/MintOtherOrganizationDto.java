package gov.samhsa.ocp.ocpmintapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MintOtherOrganizationDto {
    private String patientID;
    private String organizationName;

    @Override
    public String toString() {
        return "MintOtherOrganization{" +
                "patientID='" + patientID + '\'' +
                ", organizationName='" + organizationName + '\'' +
                '}';
    }
}
