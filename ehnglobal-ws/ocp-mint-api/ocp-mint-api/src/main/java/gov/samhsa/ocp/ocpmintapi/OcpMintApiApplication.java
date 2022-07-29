package gov.samhsa.ocp.ocpmintapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableWebSecurity
@EnableDiscoveryClient
public class OcpMintApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcpMintApiApplication.class, args);
    }

}
