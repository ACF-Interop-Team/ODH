package gov.samhsa.ocp.ocpfis.service;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.samhsa.ocp.ocpfis.config.FisProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class ClientCredentialsBearerTokenProxy implements IClientInterceptor {
    private final FisProperties fisProperties;

    public ClientCredentialsBearerTokenProxy(FisProperties fisProperties) {
        Validate.notNull(fisProperties);
        this.fisProperties = fisProperties;
    }

    @Override
    public void interceptRequest(IHttpRequest iHttpRequest) {
        String token = generateProxyToken(fisProperties);
        log.info("Bearer token: " + token);
        iHttpRequest.removeHeaders("If-Match");
        iHttpRequest.addHeader(Constants.HEADER_AUTHORIZATION, (Constants.HEADER_AUTHORIZATION_VALPREFIX_BEARER + token));
    }

    @Override
    public void interceptResponse(IHttpResponse iHttpResponse) throws IOException {

    }


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

    public String generateProxyToken(FisProperties fisProperties) {
        Gson gson = new Gson();
        JsonObject header_body = new JsonObject();
        JsonObject payload = new JsonObject();

        header_body.addProperty("alg","HS256");
        header_body.addProperty("typ","JWT");

        JsonArray role = new JsonArray();
        role.add("admin");
        role.add("user");

        JsonArray arrayJwtAudience = decodeBase64toJson(fisProperties.getFhir().getJwt_audience());

        Calendar date = Calendar.getInstance();
        long timesecs = date.getTimeInMillis();
        Date expiratino_date = new Date(timesecs + ((long) fisProperties.getFhir().getExpiration_token() * 60 * 1000));
        long epoch_exp = expiratino_date.getTime() / 1000L;

        JsonArray arrayJwtIssuer = decodeBase64toJson(fisProperties.getFhir().getJwt_issuer());
        if (arrayJwtIssuer.size() < 2) {
            String parsedFirstJwtIssuer = gson.fromJson(arrayJwtIssuer.get(0), String.class);
            payload.addProperty("iss", parsedFirstJwtIssuer);
        } else if (arrayJwtIssuer.size() > 1) {
            payload.add("iss", arrayJwtIssuer);
        }
       
        JsonArray arrayJwtSubject = decodeBase64toJson(fisProperties.getFhir().getJwt_subject());
        if (arrayJwtSubject.size() < 2) {
            String parsedFirstJwtSubject = gson.fromJson(arrayJwtSubject.get(0), String.class);
            payload.addProperty("sub", parsedFirstJwtSubject);
        } else if (arrayJwtSubject.size() > 1) {
            payload.add("sub", arrayJwtSubject);
        }

        payload.add("aud", arrayJwtAudience);
        payload.addProperty("exp", epoch_exp);
        payload.addProperty("scope", "openid email profile patient:create patient:read");
        payload.add("role", role);

        String encodedHeader = encode(header_body.toString().getBytes(StandardCharsets.UTF_8));
        String encodedBody = encode(payload.toString().getBytes(StandardCharsets.UTF_8));

        String signature = hmacSha256(encodedHeader + "." + encodedBody, fisProperties.getFhir().getJwt_secret());
        return encodedHeader + "." + encodedBody + "." + signature;
    }

    private JsonArray decodeBase64toJson(String property) {
        byte[] decodedJwtBytes = Base64.getDecoder().decode(property);
        String decodedJwt = new String(decodedJwtBytes);
        return JsonParser.parseString(decodedJwt).getAsJsonArray();
    }
}
