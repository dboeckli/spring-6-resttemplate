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
public class RestMvcHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String restMvcUrl;

    public RestMvcHealthIndicator(@Value("${rest.template.base.url}") String restMvcUrl) {
        this.restTemplate = new RestTemplateBuilder().build();
        this.restMvcUrl = restMvcUrl;
    }

    @Override
    public Health health() {
        try {
            String response = restTemplate.getForObject(restMvcUrl + "/actuator/health", String.class);
            if (response != null && response.contains("\"status\":\"UP\"")) {
                return Health.up().build();
            } else {
                log.warn("Auth server is not reporting UP status at {}", restMvcUrl);
                return Health.down().build();
            }
        } catch (Exception e) {
            log.warn("Auth server is not reachable at {}", restMvcUrl, e);
            return Health.down(e).build();
        }
    }

}
