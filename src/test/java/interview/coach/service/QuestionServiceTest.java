package interview.coach.service;

import interview.coach.api.dto.QuestionDtos.QuestionRequest;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import interview.coach.domain.entity.Question;
import interview.coach.domain.entity.Role;
import interview.coach.domain.entity.User;
import interview.coach.repository.QuestionRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void createShouldPersistQuestionWithCreatorAndDefaults() {
        AppUserPrincipal principal = new AppUserPrincipal(
                UUID.randomUUID(),
                "admin@example.com",
                "ADMIN",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        User creator = new User();
        creator.setId(principal.userId());
        Role role = new Role();
        role.setCode("ADMIN");
        creator.setRole(role);

        Question saved = new Question();
        saved.setId(UUID.randomUUID());
        saved.setText("What is JVM?");
        saved.setQuestionType(QuestionType.TECHNICAL);
        saved.setDifficulty(InterviewLevel.JUNIOR);
        saved.setDirection(InterviewDirection.BACKEND);
        saved.setStatus(QuestionStatus.ACTIVE);
        saved.setCreatedBy(creator);
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(userService.getCurrentUser(principal)).thenReturn(creator);
        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        var response = questionService.create(principal, new QuestionRequest(
                "What is JVM?",
                QuestionType.TECHNICAL,
                InterviewLevel.JUNIOR,
                InterviewDirection.BACKEND,
                null
        ));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        Question persisted = captor.getValue();
        assertThat(persisted.getCreatedBy()).isEqualTo(creator);
        assertThat(persisted.getStatus()).isEqualTo(QuestionStatus.ACTIVE);
        assertThat(response.text()).isEqualTo("What is JVM?");
        assertThat(response.createdBy()).isEqualTo(creator.getId());
    }
}
