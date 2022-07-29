package gov.samhsa.ocp.ocpmintapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(value = "ocp-mint-api.mint.enabled")
public class MintClientCredentialsConfig {
    private static String CLIENT_REGISTRATION_ID = "mint";

    private final MintOAuthProperties mintOAuthProperties;
    private final MintApiConfig mintApiConfig;

    @Autowired
    public MintClientCredentialsConfig(MintOAuthProperties mintOAuthProperties, MintApiConfig mintApiConfig) {
        this.mintOAuthProperties = mintOAuthProperties;
        this.mintApiConfig = mintApiConfig;
    }

    @Bean
    ReactiveClientRegistrationRepository getRegistration(
    ) {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId(CLIENT_REGISTRATION_ID)
                .tokenUri(mintOAuthProperties.getProvider().getMint().getTokenUri())
                .clientId(mintOAuthProperties.getRegistration().getMint().getClientId())
                .clientSecret(mintOAuthProperties.getRegistration().getMint().getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    @Bean(name = "mint")
    WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrations, new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
        oauth.setDefaultClientRegistrationId(CLIENT_REGISTRATION_ID);
        oauth.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder()
                .filter(oauth)
                .defaultHeaders(httpHeaders -> {
                    mintApiConfig.getHeaders().forEach(header -> {
                        httpHeaders.add(header.getName(), header.getValue());
                    });
                })
                .build();
    }


/*    // This method returns filter function which will log request data
    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }*/
}
