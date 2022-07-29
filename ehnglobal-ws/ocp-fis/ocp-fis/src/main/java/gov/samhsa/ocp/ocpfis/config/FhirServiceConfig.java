package gov.samhsa.ocp.ocpfis.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

@Configuration
@Slf4j
public class FhirServiceConfig {

    private final FisProperties fisProperties;

    private Optional<OAuth2RestTemplate> oAuth2RestTemplate;

    @Autowired
    public FhirServiceConfig(FisProperties fisProperties, Optional<OAuth2RestTemplate> oAuth2RestTemplate) {
        this.fisProperties = fisProperties;
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    @Bean
    public FhirContext fhirContext() {
        FhirContext fhirContext = FhirContext.forR4();
        fhirContext.getRestfulClientFactory().setSocketTimeout(Integer.parseInt(fisProperties.getFhir().getClientSocketTimeoutInMs()));
        return fhirContext;
    }

    @Bean
    public IGenericClient fhirClient() {
        return new OptionalFhirserver().getClient(fhirContext(), fisProperties);


    @Bean
    public IParser fhirJsonParser() {
        return fhirContext().newJsonParser();
    }

    @Bean
    public FhirValidator fhirValidator() {
        FhirValidator fhirValidator = fhirContext().newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(fhirContext());
        instanceValidator.setValidationSupport(fhirContext().getValidationSupport());
        instanceValidator.setErrorForUnknownProfiles(false);
        fhirValidator.registerValidatorModule(instanceValidator);
        return fhirValidator;
    }

}
