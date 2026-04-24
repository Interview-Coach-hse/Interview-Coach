package interview.coach.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(AssessmentClientProperties.class)
public class AssessmentClientConfig {

    @Bean
    RestTemplate assessmentRestTemplate(RestTemplateBuilder builder, AssessmentClientProperties properties) {
        Duration timeout = Duration.ofMillis(properties.timeoutMillis());
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
