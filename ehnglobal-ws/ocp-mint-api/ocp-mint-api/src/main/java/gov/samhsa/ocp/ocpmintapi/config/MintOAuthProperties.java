package gov.samhsa.ocp.ocpmintapi.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Data
public class MintOAuthProperties {
    @NotNull
    private Registration registration;
    @NotNull
    private Provider provider;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Registration {
        @NotNull
        private Mint mint;

        @Data
        public static class Mint {
            @NotNull
            private String clientId;
            @NotNull
            private String clientSecret;
            @NotNull
            private String authorizationGrantType;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Provider {
        @NotNull
        private Mint mint;

        @Data
        public static class Mint {
            @NotNull
            private String tokenUri;
        }
    }
}
