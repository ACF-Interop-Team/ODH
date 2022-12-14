package gov.samhsa.c2s.c2ssofapi.config;

import ca.uhn.fhir.rest.api.EncodingEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s-sof-api")
@Validated
@Data
public class ConfigProperties {

    @NotNull
    @Valid
    private Fhir fhir;

    @NotNull
    @Valid
    private Practitioner practitioner;

    @NotNull
    @Valid
    private Organization organization;

    @NotNull
    @Min(1)
    @Max(1000)
    private int ResourceSinglePageLimit;

    @NotNull
    @Valid
    private Consent consent;

    @NotNull
    @Valid
    private Patient patient;

    @NotNull
    private boolean provenanceEnabled;

    @Data
    public static class Fhir {
        @NotBlank
        private String clientSocketTimeoutInMs;
        @NotNull
        private EncodingEnum encoding = EncodingEnum.JSON;
        @NotNull
        private int defaultResourceBundlePageSize;
        @NotNull
        private String serverurl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Consent {
        @NotNull
        private String identifierSystem;

        @Valid
        private Pagination pagination = new Pagination();

        @Data
        public static class Pagination {
            @Min(1)
            @Max(500)
            private int defaultSize = 10;
            @Min(1)
            @Max(500)
            private int maxSize = 50;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Practitioner {
        @Valid
        private Pagination pagination = new Pagination();

        @Data
        public static class Pagination {
            @Min(1)
            @Max(500)
            private int defaultSize = 10;
            @Min(1)
            @Max(500)
            private int maxSize = 50;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Organization {
        @Valid
        private Pagination pagination = new Pagination();

        @Data
        public static class Pagination {
            @Min(1)
            @Max(500)
            private int defaultSize = 10;
            @Min(1)
            @Max(500)
            private int maxSize = 50;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patient {
        @Valid
        private Pagination pagination = new Pagination();

        @Valid
        private Mrn mrn = new Mrn();
        @Valid
        private SSN ssn = new SSN();
        @Valid
        private LICENSE license = new LICENSE();

        @Data
        public static class Pagination {
            @Min(1)
            @Max(500)
            private int defaultSize = 10;
            @Min(1)
            @Max(500)
            private int maxSize = 50;
        }

        @Data
        public static class SSN {
            private String codeSystem;
            private String displayName;
        }

        @Data
        public static class LICENSE {
            private String codeSystem;
            private String displayName;
        }

        @Data
        public static class Mrn {
            private String codeSystem;
            private String codeSystemOID;
            private String displayName;
            private String prefix;
            private int length;
        }
    }
}

