package gov.samhsa.ocp.ocpmintapi.service;

import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientResponseDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.PatientDto;
import reactor.core.publisher.Mono;

public interface PatientService {
    Mono<MintPatientResponseDto> getPatientById(String id);

    Mono<PatientDto> findPatient(String orgName, String firstName, String lastName, String birthDate);

    boolean saveOcpPatient(PatientDto patientDto);
}
