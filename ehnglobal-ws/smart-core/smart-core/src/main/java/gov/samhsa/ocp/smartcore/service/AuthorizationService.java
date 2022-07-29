package gov.samhsa.ocp.smartcore.service;

import gov.samhsa.ocp.smartcore.domain.ResponseType;

import java.net.URI;

public interface AuthorizationService {
    URI getRedirectUri(String clientId,
                       ResponseType responseType,
                       String scope,
                       String redirectUri,
                       String state,
                       String aud,
                       String launch);
}
