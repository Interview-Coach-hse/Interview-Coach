package interview.coach.service;

import interview.coach.api.dto.SessionDtos.CreateSessionRequest;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.InterviewSession;
import interview.coach.domain.entity.Role;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.InterviewSessionRepository;
import interview.coach.repository.ReportItemRepository;
import interview.coach.repository.SessionMessageRepository;
import interview.coach.repository.SessionReportRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewSessionServiceTest {

    @Mock
    private InterviewSessionRepository interviewSessionRepository;

    @Mock
    private SessionMessageRepository sessionMessageRepository;

    @Mock
    private SessionReportRepository sessionReportRepository;

    @Mock
    private ReportItemRepository reportItemRepository;

    @Mock
    private InterviewProfileService interviewProfileService;

    @Mock
    private UserService userService;

    @InjectMocks
    private InterviewSessionService interviewSessionService;

    @Test
    void createSessionShouldRejectWhenInProgressSessionExists() {
        AppUserPrincipal principal = principal("user@example.com", "USER");
        User user = user(principal.userId(), "USER");

        when(userService.getCurrentUser(principal)).thenReturn(user);
        when(interviewSessionRepository.existsByUserIdAndStateIn(user.getId(), Set.of(SessionState.IN_PROGRESS))).thenReturn(true);

        assertThatThrownBy(() -> interviewSessionService.createSession(principal, new CreateSessionRequest(UUID.randomUUID())))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void pauseSessionShouldMoveInProgressSessionToPaused() {
        AppUserPrincipal principal = principal("user@example.com", "USER");
        InterviewSession session = session(principal.userId(), SessionState.IN_PROGRESS);

        when(interviewSessionRepository.findByIdAndUserId(session.getId(), principal.userId())).thenReturn(Optional.of(session));
        when(interviewSessionRepository.save(any(InterviewSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = interviewSessionService.pauseSession(principal, session.getId());

        assertThat(response.action()).isEqualTo("PAUSED");
        assertThat(response.session().state()).isEqualTo(SessionState.PAUSED);
        verify(interviewSessionRepository).save(session);
    }

    private static AppUserPrincipal principal(String email, String roleCode) {
        return new AppUserPrincipal(
                UUID.randomUUID(),
                email,
                roleCode,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleCode))
        );
    }

    private static User user(UUID id, String roleCode) {
        User user = new User();
        user.setId(id);
        Role role = new Role();
        role.setCode(roleCode);
        user.setRole(role);
        return user;
    }

    private static InterviewSession session(UUID userId, SessionState state) {
        User user = user(userId, "USER");
        InterviewProfile profile = new InterviewProfile();
        profile.setId(UUID.randomUUID());
        profile.setTitle("Java Backend");
        profile.setDirection(InterviewDirection.BACKEND);
        profile.setLevel(InterviewLevel.JUNIOR);
        profile.setStatus(ProfileStatus.PUBLISHED);

        InterviewSession session = new InterviewSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setProfile(profile);
        session.setState(state);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return session;
    }
}
