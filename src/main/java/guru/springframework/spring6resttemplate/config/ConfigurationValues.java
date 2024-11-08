package guru.springframework.spring6resttemplate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ConfigurationValues {

    @Value("${spring.security.oauth2.client.registration.springauth.provider}")
    private String springAuthProviderId; 
    
}
