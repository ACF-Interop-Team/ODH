package gov.samhsa.ocp.ocpmintapi.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "ocp-mint-api.mint")
@Data
public class MintApiConfig {
    @NotNull
    private boolean enabled;
    @NotNull
    private String serverUrl;
    @NotNull
    private List<Header> headers;
    @NotNull
    private Mrn mrn;
    @NotNull
    private String defaultOrgNameHeader;
    @NotNull
    private String defaultOrgNameValue;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Header {
        @NotNull
        private String name;
        @NotNull
        private String value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Mrn {
        @NotNull
        private String prefix;
    }
}
