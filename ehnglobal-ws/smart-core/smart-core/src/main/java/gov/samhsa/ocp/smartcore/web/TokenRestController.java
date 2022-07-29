package gov.samhsa.ocp.smartcore.web;

import feign.Headers;
import gov.samhsa.ocp.smartcore.domain.GrantType;
import gov.samhsa.ocp.smartcore.infrastructure.dto.TokenResponseDto;
import gov.samhsa.ocp.smartcore.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

import static gov.samhsa.ocp.smartcore.config.Constants.AUTHORIZATION_HEADER;
import static gov.samhsa.ocp.smartcore.config.Constants.CLIENT_ID;
import static gov.samhsa.ocp.smartcore.config.Constants.CODE;
import static gov.samhsa.ocp.smartcore.config.Constants.GRANT_TYPE;
import static gov.samhsa.ocp.smartcore.config.Constants.REDIRECT_URI;

@RestController
public class TokenRestController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/token")
    public TokenResponseDto getToken(@RequestHeader(AUTHORIZATION_HEADER) Optional<String> basicAuth,
                                     @RequestParam(GRANT_TYPE) GrantType grantType,
                                     @RequestParam(CODE) String code,
                                     @RequestParam(CLIENT_ID) Optional<String> clientId,
                                     @RequestParam(REDIRECT_URI) String redirectUri) {
        return tokenService.getToken(
                basicAuth,
                grantType,
                code,
                clientId,
                redirectUri);
    }
}
