package gov.samhsa.ocp.ocpfis.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.samhsa.ocp.ocpfis.service.ClientCredentialsBearerTokenProxy;
import gov.samhsa.ocp.ocpfis.service.exception.FHIRClientException;
import gov.samhsa.ocp.ocpfis.service.exception.PreconditionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;

@Slf4j
public class OptionalFhirserver {
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String EMPTY_STRING = "";

    private String hmacSha256(String data, String secret) {
        try {
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public IGenericClient getClient(FhirContext fhirContext, FisProperties fisProperties) {
            log.info("Connecting to FHIR Proxy...");
            log.info("PROXY URL: " + fisProperties.getFhir().getServerUrl());
            return proxyclient(fhirContext, fisProperties);
    }

    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public IGenericClient proxyclient(FhirContext fhirContext, FisProperties fisProperties) {

//        final Optional<HttpServletRequest> httpServletRequest = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
//                .filter(ServletRequestAttributes.class::isInstance)
//                .map(ServletRequestAttributes.class::cast)
//                .map(ServletRequestAttributes::getRequest);
        fhirContext.getRestfulClientFactory().setSocketTimeout(Integer.parseInt(fisProperties.getFhir().getClientSocketTimeoutInMs()));
        final IGenericClient proxyclient_generic = fhirContext.newRestfulGenericClient(fisProperties.getFhir().getServerUrl());
        ClientCredentialsBearerTokenProxy clientCredentialsBearerTokenProxy = new ClientCredentialsBearerTokenProxy(fisProperties);
        proxyclient_generic.registerInterceptor(clientCredentialsBearerTokenProxy);

//        httpServletRequest
//                .map(req -> req.getHeader(HTTP_HEADER_AUTHORIZATION))
//                .filter(auth -> auth.startsWith(BEARER_PREFIX))
//                .map(auth -> auth.replace(BEARER_PREFIX, EMPTY_STRING))
//                .filter(StringUtils::hasText).ifPresent(accessToken -> {
//                    System.out.println(accessToken);
//                    proxyclient_generic.registerInterceptor(new BearerTokenAuthInterceptor(accessToken));
//                });
        return proxyclient_generic;
    }


}
