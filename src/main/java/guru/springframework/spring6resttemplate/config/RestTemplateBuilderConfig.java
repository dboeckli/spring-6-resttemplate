package guru.springframework.spring6resttemplate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateBuilderConfig {

    @Value("${rest.template.base.url}")
    private String baseUrl;

    @Value("${rest.template.user}")
    private String user;

    @Value("${rest.template.password}")
    private String password;

    @Bean
    RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer configurer){
        return configurer.configure(new RestTemplateBuilder()
            .basicAuthentication(user, password))
            .uriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }

}
