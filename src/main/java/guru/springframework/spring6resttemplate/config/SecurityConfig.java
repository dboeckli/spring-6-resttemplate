package guru.springframework.spring6resttemplate.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(99)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests -> {
                authorizeRequests
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()  // permit all actuator endpoints
                    .requestMatchers("/beers/**").permitAll()
                    .requestMatchers("/beer/**").permitAll()
                    .requestMatchers("/webjars/**").permitAll()
                    .requestMatchers("/favicon.ico").permitAll()
                    .anyRequest().authenticated();
            });
        return http.build();
    }
}
