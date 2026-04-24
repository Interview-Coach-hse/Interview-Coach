package interview.coach.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.assessment")
public record AssessmentClientProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String clientId,
        String metadataSource,
        String mode,
        int questionLimit,
        String reportLanguage,
        long timeoutMillis,
        long probeTimeoutMillis,
        long probeIntervalMillis,
        int reportPollAttempts,
        long reportPollDelayMillis,
        int circuitBreakerFailureThreshold,
        long circuitBreakerOpenDurationSeconds
) {
}
