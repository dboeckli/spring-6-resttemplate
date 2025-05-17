package guru.springframework.spring6resttemplate.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AuthServerHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String authServerUrl;

    public AuthServerHealthIndicator(@Value("${security.auth-server-health-url}") String authServerUrl) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.authServerUrl = authServerUrl;
    }

    @Override
    public Health health() {
        try {
            String response = restTemplate.getForObject(authServerUrl + "/actuator/health", String.class);
            if (response != null && response.contains("\"status\":\"UP\"")) {
                return Health.up().build();
            } else {
                log.warn("Auth server is not reporting UP status at {}", authServerUrl);
                return Health.down().build();
            }
        } catch (Exception e) {
            log.warn("Auth server is not reachable at {}", authServerUrl, e);
            return Health.down(e).build();
        }
    }

}
