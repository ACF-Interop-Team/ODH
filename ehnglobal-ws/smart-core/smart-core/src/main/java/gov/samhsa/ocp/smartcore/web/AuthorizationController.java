package gov.samhsa.ocp.smartcore.web;

import gov.samhsa.ocp.smartcore.domain.ResponseType;
import gov.samhsa.ocp.smartcore.service.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

import static gov.samhsa.ocp.smartcore.config.Constants.AUD;
import static gov.samhsa.ocp.smartcore.config.Constants.CLIENT_ID;
import static gov.samhsa.ocp.smartcore.config.Constants.LAUNCH;
import static gov.samhsa.ocp.smartcore.config.Constants.REDIRECT_PREFIX;
import static gov.samhsa.ocp.smartcore.config.Constants.REDIRECT_URI;
import static gov.samhsa.ocp.smartcore.config.Constants.RESPONSE_TYPE;
import static gov.samhsa.ocp.smartcore.config.Constants.SCOPE;
import static gov.samhsa.ocp.smartcore.config.Constants.STATE;

@Controller
@Slf4j
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/authorize")
    public String getAuthorization(
            @RequestParam(CLIENT_ID) String clientId,
            @RequestParam(RESPONSE_TYPE) ResponseType responseType,
            @RequestParam(SCOPE) String scope,
            @RequestParam(REDIRECT_URI) String redirectUri,
            @RequestParam(STATE) String state,
            @RequestParam(AUD) String aud,
            @RequestParam(LAUNCH) String launch) {
        final URI authRedirectUri = authorizationService.getRedirectUri(clientId,
                responseType,
                scope,
                redirectUri,
                state,
                aud,
                launch);
        System.out.println("AUTHORIZE URL: "+authRedirectUri.toString());
        return REDIRECT_PREFIX + authRedirectUri.toString();
    }
}
