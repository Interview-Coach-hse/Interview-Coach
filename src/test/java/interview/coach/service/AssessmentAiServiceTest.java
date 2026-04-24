package interview.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.coach.config.AssessmentClientProperties;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.domain.entity.ProfileQuestion;
import interview.coach.domain.entity.Question;
import interview.coach.repository.ProfileQuestionRepository;
import interview.coach.repository.ProfileTagRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    void shouldLoadNextPromptFromProfileQuestionsWithoutCallingAssessmentApi() {
        AssessmentClientProperties properties = new AssessmentClientProperties(
                true,
                "http://localhost:8000",
                "demo-api-key",
                "main-backend",
                "main-backend",
                "sync",
                10,
                "ru",
                900000,
                5000,
                60000,
                5,
                10,
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

        InterviewSession session = session();
        ProfileQuestion profileQuestion = new ProfileQuestion();
        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setText("What is Spring Boot?");
        question.setQuestionType(QuestionType.TECHNICAL);
        question.setDirection(InterviewDirection.BACKEND);
        question.setDifficulty(InterviewLevel.JUNIOR);
        question.setStatus(QuestionStatus.ACTIVE);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        profileQuestion.setQuestion(question);
        profileQuestion.setOrderIndex(0);
        profileQuestion.setRequired(true);

        when(profileQuestionRepository.findByProfileIdOrderByOrderIndexAsc(session.getProfile().getId()))
                .thenReturn(List.of(profileQuestion));

        var prompt = assessmentAiService.getNextPrompt(session, 0);

        assertThat(prompt.content()).isEqualTo("What is Spring Boot?");
        assertThat(prompt.questionExternalId()).isEqualTo(question.getId().toString());
        verify(assessmentRestTemplate, never()).exchange(any(org.springframework.http.RequestEntity.class), any(Class.class));
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
