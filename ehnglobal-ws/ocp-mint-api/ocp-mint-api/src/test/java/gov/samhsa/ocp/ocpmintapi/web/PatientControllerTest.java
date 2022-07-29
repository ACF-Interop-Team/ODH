package gov.samhsa.ocp.ocpmintapi.web;

import gov.samhsa.ocp.ocpmintapi.service.PatientService;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintDemographicsDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintOtherOrganizationDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PatientControllerTest {
    private WebTestClient webTestClient;
    private List<MintPatientResponseDto> expectedList;
    @MockBean
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToController(new PatientController(patientService))
                .configureClient().baseUrl("/patients").build();

        this.expectedList = Arrays.asList(
                MintPatientResponseDto.builder().demographics(
                        MintDemographicsDto.builder().birthdate("1972-01-01")
                                .familyName("Duck")
                                .givenName("Donna")
                                .sex("F").build()
                ).otherOrganizations(
                        Arrays.asList(
                                MintOtherOrganizationDto.builder().patientID("donna000001.omni")
                                        .organizationName("Howard").build()
                        )
                ).build(),
                MintPatientResponseDto.builder().demographics(
                        MintDemographicsDto.builder().birthdate("1975-01-01")
                                .familyName("Duck")
                                .givenName("Donald")
                                .sex("M").build()
                ).otherOrganizations(
                        Arrays.asList(
                                MintOtherOrganizationDto.builder().patientID("1")
                                        .organizationName("Howard").build()
                        )
                ).build());
    }


    @Test
    void testGetPatientByIdNotFound() {
        String id = "123";
        when(patientService.getPatientById(id)).thenReturn(Mono.empty());
        webTestClient.get().uri(id).exchange().expectStatus().isNotFound().expectBody(Void.class);
        ;
    }

    @Test
    void testGetPatientByIdFound() {
        MintPatientResponseDto expectedMintPatientDto = expectedList.get(1);
        when(patientService.getPatientById(expectedMintPatientDto.getOtherOrganizations().get(0).getPatientID()))
                .thenReturn(Mono.just(expectedMintPatientDto));
        webTestClient.get().uri("/{id}", expectedMintPatientDto.getOtherOrganizations().get(0).getPatientID())
                .exchange().expectStatus().isOk()
                .expectBody(MintPatientResponseDto.class).isEqualTo(expectedMintPatientDto);
    }
    

}