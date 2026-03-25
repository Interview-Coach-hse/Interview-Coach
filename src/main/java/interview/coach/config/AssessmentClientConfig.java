package interview.coach.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(AssessmentClientProperties.class)
public class AssessmentClientConfig {

    @Bean
    RestTemplate assessmentRestTemplate() {
        return new RestTemplate();
    }
}
