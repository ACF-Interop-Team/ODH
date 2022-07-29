package gov.samhsa.ocp.ocpmintapi.service.util;

import gov.samhsa.ocp.ocpmintapi.service.dto.AddressDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintDemographicsDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintOtherOrganizationDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientRequestDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientResponseDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.NameDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.PatientDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.ReferenceDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.TelecomDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MintPatientDtoToPatientDtoMap {

    protected PatientDto convertToPatientDto(MintPatientDto mintPatientDto) {

        NameDto nameDto = NameDto.builder().firstName(mintPatientDto.getDemographics().getGivenName())
                .lastName(mintPatientDto.getDemographics().getFamilyName())
                .build();
        AddressDto addressDto = AddressDto.builder().line1(mintPatientDto.getDemographics().getStreetAddress())
                .city(mintPatientDto.getDemographics().getCity())
                .stateCode(mintPatientDto.getDemographics().getState())
                .postalCode(mintPatientDto.getDemographics().getZip())
                .build();
        List<TelecomDto> telecomDtoList = new ArrayList<>();

        if (null != mintPatientDto.getDemographics().getEmailBusiness() ||
                null != mintPatientDto.getDemographics().getEmailHome() ||
                null != mintPatientDto.getDemographics().getHomePhone() ||
                null != mintPatientDto.getDemographics().getBusinessPhone()) {
            telecomDtoList = setPatientTelecom(mintPatientDto.getDemographics());
        }

        List<ReferenceDto> organizationList = new ArrayList<>();
        if (null != mintPatientDto.getOtherOrganizations()) {
            organizationList = setPatientOrganization(mintPatientDto.getOtherOrganizations());
        }

        return PatientDto.builder()
                .name(Collections.singletonList(nameDto))
                .addresses(Collections.singletonList(addressDto))
                .birthDate(setBirthDate(mintPatientDto.getDemographics().getBirthdate()))
                .birthSex(mintPatientDto.getDemographics().getSex())
                .genderCode(mintPatientDto.getDemographics().getSex())
                .telecoms(telecomDtoList)
                .organizations(Optional.of(organizationList))
                .race(mintPatientDto.getDemographics().getRace())
                .build();
    }

    private List<ReferenceDto> setPatientOrganization(List<MintOtherOrganizationDto> otherOrganizations) {
     /*
     *             "otherOrganizations": [
                    {
                        "organizationName": "Hackensack Meridian",
                        "patientID": "OCP-DEV-S7MDZV9QKZ"
                    }
                ]
     * */
        List<ReferenceDto> organizations = new ArrayList<>();
        for (MintOtherOrganizationDto mintOrgDto : otherOrganizations) {
            organizations.add(ReferenceDto.builder().id(mintOrgDto.getPatientID())
                    .display(mintOrgDto.getOrganizationName())
                    .reference(mintOrgDto.getOrganizationName()).build());

        }

        return organizations;
    }


    private LocalDate setBirthDate(String birthDate) {
        String DATE_STRING = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_STRING);
        //convert String to LocalDate
        return LocalDate.parse(birthDate, formatter);
    }

    public PatientDto convertToPatientResponseDto(MintPatientResponseDto mintPatientDto, String mrnPrefix) {

        NameDto nameDto = NameDto.builder().firstName(mintPatientDto.getDemographics().getGivenName())
                .lastName(mintPatientDto.getDemographics().getFamilyName())
                .build();
        AddressDto addressDto = AddressDto.builder().line1(mintPatientDto.getDemographics().getStreetAddress())
                .city(mintPatientDto.getDemographics().getCity())
                .stateCode(mintPatientDto.getDemographics().getState())
                .countryCode("US")
                .postalCode(mintPatientDto.getDemographics().getZip())
                .build();
        List<TelecomDto> telecomDtoList = new ArrayList<>();
        if (null != mintPatientDto.getDemographics().getEmailBusiness() ||
                null != mintPatientDto.getDemographics().getEmailHome() ||
                null != mintPatientDto.getDemographics().getHomePhone() ||
                null != mintPatientDto.getDemographics().getBusinessPhone()) {
            telecomDtoList = setPatientTelecom(mintPatientDto.getDemographics());
        }

        List<ReferenceDto> organizationList = new ArrayList<>();
        if (null != mintPatientDto.getOtherOrganizations()) {
            organizationList = setPatientOrganization(mintPatientDto.getOtherOrganizations());
        }
        Optional<String> ocpMrn = getOcpMrn(mintPatientDto, mrnPrefix);
        return PatientDto.builder()
                .name(Collections.singletonList(nameDto))
                .addresses(Collections.singletonList(addressDto))
                .birthDate(setBirthDate(mintPatientDto.getDemographics().getBirthdate()))
                .birthSex(getSexValue(mintPatientDto.getDemographics().getSex()))
                .genderCode(getGenderValue(mintPatientDto.getDemographics().getSex()))
                .telecoms(telecomDtoList)
                .mrn(ocpMrn)
                .organizations(Optional.of(organizationList))
                .active(Boolean.TRUE)
                .build();
    }

    private Optional<String> getOcpMrn(MintPatientResponseDto mintPatientDto, String mrnPrefix) {
        Optional<String> ocpMrn = Optional.empty();
        if (null != mintPatientDto.getPatientID() && mintPatientDto.getPatientID().contains(mrnPrefix)) {
            // patient is present in the same organization of OCP
            ocpMrn = Optional.of(mintPatientDto.getPatientID());
        } else {
            Optional<MintOtherOrganizationDto> ocpMintPatientOrgDto = mintPatientDto.getOtherOrganizations().stream().filter(
                    mintOtherOrganizationDto -> mintOtherOrganizationDto.getPatientID() != null && mintOtherOrganizationDto.getPatientID().contains(mrnPrefix)).findFirst();
            if (ocpMintPatientOrgDto.isPresent()) {
                ocpMrn = Optional.of(ocpMintPatientOrgDto.get().getPatientID());
            }
        }
        return ocpMrn;
    }

    public MintPatientResponseDto convertToMintPatientResponseDto(PatientDto patientDto) {
        AddressDto addressDto = patientDto.getAddresses().get(0);
        ReferenceDto organization = patientDto.getOrganizations().get().get(0);
        MintOtherOrganizationDto mintOtherOrganizationDto = MintOtherOrganizationDto.builder()
                .patientID(organization.getId())
                .organizationName(organization.getDisplay()).build();

        MintPatientResponseDto mintPatientDto = MintPatientResponseDto.builder().demographics(
                MintDemographicsDto.builder().birthdate(null != patientDto.getBirthDate() ? patientDto.getBirthDate().toString() : "")
                        .familyName(null != patientDto.getName() ? patientDto.getName().get(0).getLastName() : "")
                        .givenName(null != patientDto.getName() ? patientDto.getName().get(0).getFirstName() : "")
                        .sex(getSexValue(patientDto.getGenderCode()))
                        .city(addressDto.getCity())
                        .race(patientDto.getRace())
                        .streetAddress(addressDto.getLine1())
                        .state(addressDto.getStateCode())
                        .zip(addressDto.getPostalCode()).build()
        ).otherOrganizations(
                Arrays.asList(mintOtherOrganizationDto)
        ).build();

        // set email and phone information
        setMintDemographicsTelecom(patientDto.getTelecoms(), mintPatientDto.getDemographics());

        return mintPatientDto;
    }

    public MintPatientRequestDto convertToMintPatientRequestDto(PatientDto patientDto) {
        AddressDto addressDto = patientDto.getAddresses().get(0);
        ReferenceDto organization = patientDto.getOrganizations().get().get(0);
        MintOtherOrganizationDto mintOtherOrganizationDto = MintOtherOrganizationDto.builder()
                .patientID(organization.getId())
                .organizationName(organization.getDisplay()).build();

        MintPatientRequestDto mintPatientDto = MintPatientRequestDto.builder().demographics(
                MintDemographicsDto.builder().birthdate(null != patientDto.getBirthDate() ? patientDto.getBirthDate().toString() : "")
                        .familyName(null != patientDto.getName() ? patientDto.getName().get(0).getLastName() : "")
                        .givenName(null != patientDto.getName() ? patientDto.getName().get(0).getFirstName() : "")
                        .sex(getSexValue(patientDto.getGenderCode()))
                        .city(addressDto.getCity())
                        .race(patientDto.getRace())
                        .state(addressDto.getStateCode()).streetAddress(addressDto.getLine1()).zip(addressDto.getPostalCode()).build()
        ).patientID(
                patientDto.getMrn().get()
        ).build();

        // set email and phone information
        setMintDemographicsTelecom(patientDto.getTelecoms(), mintPatientDto.getDemographics());

        return mintPatientDto;
    }

    private void setMintDemographicsTelecom(List<TelecomDto> telecomDtos, MintDemographicsDto mintDemographicsDto) {

        for (TelecomDto t : telecomDtos) {
            if (t.getSystem().isPresent() && t.getSystem().get().equalsIgnoreCase("email")) {
                if (t.getUse().isPresent() && t.getUse().get().equalsIgnoreCase("home")) {
                    mintDemographicsDto.setEmailHome(t.getValue().isPresent() ? t.getValue().get() : " ");
                } else if (t.getUse().isPresent() && t.getUse().get().equalsIgnoreCase("work")) {
                    mintDemographicsDto.setEmailBusiness(t.getValue().isPresent() ? t.getValue().get() : " ");
                }
            } else if (t.getSystem().isPresent() && t.getSystem().get().equalsIgnoreCase("phone")) {
                if (t.getUse().isPresent() && t.getUse().get().equalsIgnoreCase("home")) {
                    mintDemographicsDto.setHomePhone(t.getValue().isPresent() ? t.getValue().get() : " ");
                } else if (t.getUse().isPresent() && t.getUse().get().equalsIgnoreCase("work")) {
                    mintDemographicsDto.setBusinessPhone(t.getValue().isPresent() ? t.getValue().get() : " ");
                }
            }
        }
    }


    private List<TelecomDto> setPatientTelecom(MintDemographicsDto mintDemographicsDto) {
/*        "telecom": [
        {
            "system": "phone",
                "value": "555-212-5050",
                "use": "work",
                "rank": 1
        },
        {
            "system": "email",
                "value": "seymour.patients@mailinator.com",
                "use": "work",
                "rank": 2
        }*/
        List<TelecomDto> telecomDtos = new ArrayList<TelecomDto>();
        if (null != mintDemographicsDto.getEmailHome()) {
            String teleComValue = mintDemographicsDto.getEmailHome();
            if (!"N/A".equalsIgnoreCase(teleComValue) && !teleComValue.equalsIgnoreCase(" ")) {
                telecomDtos.add(TelecomDto.builder().system(Optional.of("email"))
                        .value(Optional.ofNullable(teleComValue))
                        .use(Optional.of("home")).build());
            }
        }
        if (null != mintDemographicsDto.getEmailBusiness()) {
            String teleComValue = mintDemographicsDto.getEmailBusiness();
            if (!"N/A".equalsIgnoreCase(teleComValue) && !teleComValue.equalsIgnoreCase(" ")) {
                telecomDtos.add(TelecomDto.builder().system(Optional.of("email"))
                        .value(Optional.ofNullable(teleComValue))
                        .use(Optional.of("work")).build());
            }
        }
        if (null != mintDemographicsDto.getHomePhone()) {
            String teleComValue = mintDemographicsDto.getHomePhone();
            if (!"N/A".equalsIgnoreCase(teleComValue) && !teleComValue.equalsIgnoreCase(" ")) {
                telecomDtos.add(TelecomDto.builder().system(Optional.of("phone"))
                        .value(Optional.ofNullable(teleComValue))
                        .use(Optional.of("home")).build());
            }
        }
        if (null != mintDemographicsDto.getBusinessPhone()) {
            String teleComValue = mintDemographicsDto.getBusinessPhone();
            if (!"N/A".equalsIgnoreCase(teleComValue) && !teleComValue.equalsIgnoreCase(" ")) {
                telecomDtos.add(TelecomDto.builder().system(Optional.of("phone"))
                        .value(Optional.ofNullable(teleComValue))
                        .use(Optional.of("work")).build());
            }
        }
        log.debug("************");
        telecomDtos.stream().forEach(telecomDto -> {
            log.debug(telecomDto.toString());
        });
        log.debug("************");
        return telecomDtos;
    }

    private String getSexValue(String codeString) {
        switch (codeString.toUpperCase()) {
            case "MALE":
                return "M";
            case "FEMALE":
                return "F";
            case "OTHER":
                return "O";
            case "UNKNOWN":
                return "U";
            default:
                return codeString.toUpperCase();
        }
    }

    private String getGenderValue(String codeString) {
        switch (codeString.toUpperCase()) {
            case "M":
                return "male";
            case "F":
                return "female";
            case "O":
                return "other";
            case "U":
                return "unknown";
            default:
                return codeString.toUpperCase();
        }
    }
}
