package interview.coach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.assessment")
public record AssessmentClientProperties(
        boolean enabled,
        String baseUrl,
        String clientId,
        String mode,
        int questionLimit,
        String reportLanguage,
        String subscriptionPlan
) {
}
