package gov.samhsa.ocp.ocpmintapi.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlagDto {
    private String logicalId;

    private String status;
    private String statusDisplay;

    private String category;
    private String categoryDisplay;

    private String code;

    private String subject;

    private PeriodDto period;

    private ReferenceDto author;
}
