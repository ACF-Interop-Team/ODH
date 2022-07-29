package gov.samhsa.ocp.ocpmintapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelecomDto {

    @NotBlank
    private Optional<String> system;

    @NotBlank
    private Optional<String> value;

    private Optional<String> use;
}
