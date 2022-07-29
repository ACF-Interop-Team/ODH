package gov.samhsa.ocp.ocpmintapi.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@RestController
public class OAuth2ClientController {

    private final WebClient webClient;

    @Value("${ocp-mint-api.mint.serverUrl}")
    private String serverUrl;

    public OAuth2ClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/test")
    public String index(@RegisteredOAuth2AuthorizedClient("mint") OAuth2AuthorizedClient authorizedClient) {
        return webClient
                .get()
                .uri(serverUrl)
                .attributes(oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
