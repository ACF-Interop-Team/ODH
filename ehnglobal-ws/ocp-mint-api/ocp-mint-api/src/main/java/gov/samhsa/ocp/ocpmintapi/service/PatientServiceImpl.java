package gov.samhsa.ocp.ocpmintapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import gov.samhsa.ocp.ocpmintapi.config.MintApiConfig;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintOtherOrganizationDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientRequestDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientResponseDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintRootDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.PatientDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.ReferenceDto;
import gov.samhsa.ocp.ocpmintapi.service.exception.BadRequestException;
import gov.samhsa.ocp.ocpmintapi.service.exception.MintServerNotFoundException;
import gov.samhsa.ocp.ocpmintapi.service.exception.MintServiceException;
import gov.samhsa.ocp.ocpmintapi.service.exception.PatientNotFoundException;
import gov.samhsa.ocp.ocpmintapi.service.util.MintPatientDtoToPatientDtoMap;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;

@Service
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final WebClient webClient;
    private final MintPatientDtoToPatientDtoMap utilPatientDto;
    private final ModelMapper modelMapper;
    private final MintApiConfig mintApiConfig;


    public PatientServiceImpl(WebClient webClient, ModelMapper modelMapper, MintPatientDtoToPatientDtoMap utilPatientDto, ModelMapper modelMapper1, MintApiConfig mintApiConfig) {
        this.webClient = webClient;
        this.utilPatientDto = utilPatientDto;
        this.modelMapper = modelMapper1;
        this.mintApiConfig = mintApiConfig;
    }

    @Override
    public Mono<MintPatientResponseDto> getPatientById(String id) {
        Mono<MintPatientResponseDto> patientDtoMono = Mono.empty();
        StringBuffer queryString = new StringBuffer(mintApiConfig.getServerUrl());
        if (null != id) {
            queryString.append("?patientID=").append(id);
        }
        String patientDtoStr = webClient
                .get()
                .uri(String.valueOf(queryString))
                .retrieve()
                .bodyToMono(String.class).block();
        log.debug("Result returned from Find API" + patientDtoStr);

        try {
            MintRootDto mintRootDto = new ObjectMapper().readValue(patientDtoStr, MintRootDto.class);
            Optional<MintPatientResponseDto> patientDtoOptional = mintRootDto.getPatients().stream().filter(patientDto1 -> patientDto1.getOtherOrganizations().get(0).getPatientID().equals(id)).findFirst();

            patientDtoMono = patientDtoOptional.isPresent() ? Mono.just(patientDtoOptional.get()) : Mono.empty();
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
            throw new MintServiceException("Status code" + jpe.getMessage() + "id passed " + id);
        }
        return patientDtoMono;
    }

    @Override
    public boolean saveOcpPatient(PatientDto patientDto) {
        //convert fis-patientdto to mintpatientdto
        MintPatientRequestDto mintPatientDto = utilPatientDto.convertToMintPatientRequestDto(patientDto);
        String ocpOrgName = getOrgnizationName(patientDto.getOrganizations());
        boolean isPatientCreated = false;

        MintPatientDto mintPatientDto1 = null;
        String resultString = "";
        try {
            Gson gson = new Gson();
            String mintPatientDtoJson = gson.toJson(mintPatientDto);
            log.debug("patientdto json", mintPatientDtoJson);
            resultString = webClient
                    .post()
                    .uri(mintApiConfig.getServerUrl())
                    .header(mintApiConfig.getDefaultOrgNameHeader(), ocpOrgName)
                    .body(Mono.just(mintPatientDto), MintPatientDto.class)
                    .retrieve().onStatus(HttpStatus::is4xxClientError, response -> Mono.just(new BadRequestException("Status code" + response.rawStatusCode() + mintPatientDto.toString())))
                    .onStatus(HttpStatus::is5xxServerError, response -> Mono.just(new MintServiceException("Status code" + response.rawStatusCode() + mintPatientDto.toString())))
                    .bodyToMono(String.class).block();
            isPatientCreated = true;
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            log.debug("WebClientResponseException" + e.getMessage());
            throw new MintServiceException("Status code" + e.getMessage() + mintPatientDto.toString());
        } catch (BadRequestException bre) {
            bre.printStackTrace();
            log.debug("BadRequestException" + bre.getMessage() + "org name not found in MINT" + ocpOrgName);
            throw new MintServiceException("Status code" + bre.getMessage() + mintPatientDto.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("Exception" + e.getMessage());
            throw new MintServerNotFoundException("Error Connecting to Mint Sever" + mintApiConfig.getServerUrl());
        }
        log.debug("Result returned from Save API" + resultString + "PatientCreated flag" + isPatientCreated);

        return isPatientCreated;
    }

    private String getOrgnizationName(Optional<List<ReferenceDto>> referenceDto) {
        String orgName = "";
        if (referenceDto.isPresent() && null != referenceDto.get().get(0)) {
            orgName = referenceDto.get().get(0).getDisplay();
        }
        // set to default org name if its not sent from UI
        if (orgName.isEmpty()) {
            orgName = mintApiConfig.getDefaultOrgNameValue();
        }
        return orgName;
    }

    @Override
    public Mono<PatientDto> findPatient(String orgName, String firstName, String lastName, String birthDate) {
        Mono<PatientDto> patientDtoMono = Mono.empty();

        StringBuffer queryString = new StringBuffer(mintApiConfig.getServerUrl());
        if (null != firstName) {
            queryString.append("?givenName=" + firstName);
        }
        if (null != lastName) {
            queryString.append("&familyName=" + lastName);
        }
        if (null != birthDate) {
            queryString.append("&birthdate=" + birthDate);
        }
        queryString.append("&includeOtherOrganizations=true");

        String patientDtoStr = "";
        try {
            patientDtoStr = webClient
                    .get()
                    .uri(String.valueOf(queryString))
                    .header(mintApiConfig.getDefaultOrgNameHeader(), orgName)
                    .retrieve()
                    .bodyToMono(String.class).block();
        } catch (NestedRuntimeException nre) {
            if (nre instanceof WebClientResponseException && ((BadRequest) nre).getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                log.debug("bad request " + nre.getMessage());
                throw new BadRequestException(nre.getMessage());
            } else {
                log.debug("nested runtime exception " + nre.getMessage());
            }
        }
        log.debug("Result returned from Find API" + patientDtoStr);
        MintRootDto mintRootDto = null;
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mintRootDto = mapper.readValue(patientDtoStr, MintRootDto.class);
            if (null != mintRootDto) {
                if (mintRootDto.getWarning() != null) {
                    log.debug("Patient not found in Mint System: Brand New");
                    throw new PatientNotFoundException(mintRootDto.getWarning());
                }
                log.debug("MRI PRefix in MINT " + mintApiConfig.getMrn().getPrefix());
                Optional<MintPatientResponseDto> mintPatientDtoOptional = mintRootDto.getPatients().stream()
                        .filter(patientDto1 -> patientDto1.getDemographics().getFamilyName().equalsIgnoreCase(lastName)
                                && patientDto1.getDemographics().getGivenName().equalsIgnoreCase(firstName)).findFirst();
                Optional<MintOtherOrganizationDto> ocpMintPatientResponseDto = mintPatientDtoOptional.get().getOtherOrganizations().stream().filter(
                        mintOtherOrganizationDto -> mintOtherOrganizationDto.getPatientID() != null && mintOtherOrganizationDto.getPatientID().contains(mintApiConfig.getMrn().getPrefix())).findFirst();


                if (!ocpMintPatientResponseDto.isPresent()) {
                    mintPatientDtoOptional = mintRootDto.getPatients().stream().filter(patientDto1 -> patientDto1.getDemographics().getFamilyName().equalsIgnoreCase(lastName) && patientDto1.getDemographics().getGivenName().equalsIgnoreCase(firstName))
                            .findFirst();
                    log.debug("Patient check for existing in MINT as its not in MINT OCP ORG");
                    if (mintPatientDtoOptional.isPresent()) {
                        log.debug("Patient existing in MINT but not in MINT OCP ORG");
                    }
                } else {
                    log.debug("Patient exist in MINT OCP Organization with MRN as " + ocpMintPatientResponseDto.get().getPatientID());
                }
                // convert MintPatientDto to PatientDto
                if (mintPatientDtoOptional.isPresent()) {
                    patientDtoMono = Mono.just(utilPatientDto.convertToPatientResponseDto(mintPatientDtoOptional.get(), mintApiConfig.getMrn().getPrefix()));
                } else {
                    log.debug("Patient does not exist in MINT ");
                }

            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.debug("Mint call error" + e.getMessage());
            throw new MintServiceException("Status code" + e.getMessage());
        }

        return patientDtoMono;

    }


}
