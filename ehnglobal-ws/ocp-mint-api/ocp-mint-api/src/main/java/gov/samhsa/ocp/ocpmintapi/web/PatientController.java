package gov.samhsa.ocp.ocpmintapi.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.samhsa.ocp.ocpmintapi.service.PatientService;
import gov.samhsa.ocp.ocpmintapi.service.dto.MintPatientResponseDto;
import gov.samhsa.ocp.ocpmintapi.service.dto.PatientDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/patients")
public class PatientController {

    private PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<MintPatientResponseDto>> getPatientById(@PathVariable("id") String id) throws JsonProcessingException {
        return patientService.getPatientById(id).map(patientDto -> ResponseEntity.ok(patientDto)).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "find")
    public Mono<ResponseEntity<PatientDto>> findPatient(@RequestParam(value = "orgName") String orgName,
                                                        @RequestParam(value = "firstName") String firstName,
                                                        @RequestParam(value = "lastName") String lastName,
                                                        @RequestParam(value = "birthDate") String birthDate) {
        return patientService.findPatient(orgName, firstName, lastName, birthDate).map(patientDto -> ResponseEntity.ok(patientDto)).defaultIfEmpty(ResponseEntity.notFound().build());

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public boolean saveOcpPatient(@RequestBody PatientDto patientDto) {
        return patientService.saveOcpPatient(patientDto);
    }

}
