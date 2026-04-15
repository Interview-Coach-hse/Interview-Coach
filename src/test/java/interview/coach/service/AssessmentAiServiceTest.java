package interview.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.coach.config.AssessmentClientProperties;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.exception.AssessmentIntegrationException;
import interview.coach.repository.ProfileQuestionRepository;
import interview.coach.repository.ProfileTagRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentAiServiceTest {

    @Mock
    private RestTemplate assessmentRestTemplate;

    @Mock
    private ProfileQuestionRepository profileQuestionRepository;

    @Mock
    private ProfileTagRepository profileTagRepository;

    @Test
    void shouldOpenCircuitBreakerAfterConfiguredFailuresAndShortCircuitNextRequest() {
        AssessmentClientProperties properties = new AssessmentClientProperties(
                true,
                "http://localhost:8000",
                "main-backend",
                "sync",
                10,
                "ru",
                "start",
                2,
                60
        );
        Clock clock = Clock.fixed(Instant.parse("2026-04-15T12:00:00Z"), ZoneOffset.UTC);
        AssessmentCircuitBreakerService circuitBreakerService = new AssessmentCircuitBreakerService(properties, clock);
        AssessmentAiService assessmentAiService = new AssessmentAiService(
                assessmentRestTemplate,
                properties,
                profileQuestionRepository,
                profileTagRepository,
                new ObjectMapper(),
                circuitBreakerService
        );

        when(assessmentRestTemplate.getForObject(anyString(), any(Class.class), any(), any(), anyInt()))
                .thenThrow(new RestClientException("boom"));

        InterviewSession session = session();

        assertThatThrownBy(() -> assessmentAiService.getNextPrompt(session, 0))
                .isInstanceOf(AssessmentIntegrationException.class);
        assertThatThrownBy(() -> assessmentAiService.getNextPrompt(session, 0))
                .isInstanceOf(AssessmentIntegrationException.class);
        assertThatThrownBy(() -> assessmentAiService.getNextPrompt(session, 0))
                .isInstanceOf(AssessmentIntegrationException.class)
                .hasMessageContaining("Circuit breaker is open");

        verify(assessmentRestTemplate, times(2))
                .getForObject(anyString(), any(Class.class), any(), any(), anyInt());
    }

    private static InterviewSession session() {
        InterviewProfile profile = new InterviewProfile();
        profile.setId(UUID.randomUUID());
        profile.setTitle("Backend");
        profile.setDirection(InterviewDirection.BACKEND);
        profile.setLevel(InterviewLevel.JUNIOR);
        profile.setStatus(ProfileStatus.PUBLISHED);

        InterviewSession session = new InterviewSession();
        session.setId(UUID.randomUUID());
        session.setProfile(profile);
        return session;
    }
}
