package gov.samhsa.ocp.ocpmintapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MintDemographicsDto {

    private String city;
    private String familyName;
    private String givenName;
    private String birthdate;
    private String sex;
    private String race;

    private String streetAddress;
    private String state;
    private String zip;

    //Optional fields mapping
    @Builder.Default
    private String emailBusiness = " ";
    @Builder.Default
    private String emailHome = " ";
    @Builder.Default
    private String homePhone = " ";
    @Builder.Default
    private String businessPhone = " ";

    //Optional fields no mapping
    @Builder.Default
    private boolean multipleBirth = false;
    @Builder.Default
    private boolean deathIndicator = false;

    @Override
    public String toString() {
        return "MintDemographicsDto{" +
                "city='" + city + '\'' +
                ", familyName='" + familyName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", sex='" + sex + '\'' +
                ", race='" + race + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", emailBusiness='" + emailBusiness + '\'' +
                ", emailHome='" + emailHome + '\'' +
                ", homePhone='" + homePhone + '\'' +
                ", businessPhone='" + businessPhone + '\'' +
                ", multipleBirth=" + multipleBirth +
                ", deathIndicator=" + deathIndicator +
                '}';
    }
}

