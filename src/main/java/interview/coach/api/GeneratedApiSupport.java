package interview.coach.api;

import interview.coach.api.dto.AdminUserDtos;
import interview.coach.api.dto.AuthDtos;
import interview.coach.api.dto.PageDtos;
import interview.coach.api.dto.ProfileDtos;
import interview.coach.api.dto.ProgressDtos;
import interview.coach.api.dto.QuestionDtos;
import interview.coach.api.dto.UserDtos;
import interview.coach.domain.DomainEnums;
import interview.coach.security.AppUserPrincipal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

final class GeneratedApiSupport {

    private GeneratedApiSupport() {
    }

    static AppUserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            return null;
        }
        return principal;
    }

    static interview.coach.generated.model.RegisterRequest toGenerated(AuthDtos.RegisterRequest source) {
        return new interview.coach.generated.model.RegisterRequest(source.email(), source.password());
    }

    static AuthDtos.RegisterRequest toInternal(interview.coach.generated.model.RegisterRequest source) {
        return new AuthDtos.RegisterRequest(source.getEmail(), source.getPassword());
    }

    static AuthDtos.EmailVerificationRequest toInternal(interview.coach.generated.model.EmailVerificationRequest source) {
        return new AuthDtos.EmailVerificationRequest(source.getEmail());
    }

    static AuthDtos.EmailVerificationConfirmRequest toInternal(interview.coach.generated.model.EmailVerificationConfirmRequest source) {
        return new AuthDtos.EmailVerificationConfirmRequest(source.getEmail(), source.getCode());
    }

    static AuthDtos.LoginRequest toInternal(interview.coach.generated.model.LoginRequest source) {
        return new AuthDtos.LoginRequest(source.getEmail(), source.getPassword());
    }

    static AuthDtos.RefreshTokenRequest toInternal(interview.coach.generated.model.RefreshTokenRequest source) {
        return new AuthDtos.RefreshTokenRequest(source.getRefreshToken());
    }

    static AuthDtos.PasswordResetRequest toInternal(interview.coach.generated.model.PasswordResetRequest source) {
        return new AuthDtos.PasswordResetRequest(source.getEmail());
    }

    static AuthDtos.PasswordResetConfirmRequest toInternal(interview.coach.generated.model.PasswordResetConfirmRequest source) {
        return new AuthDtos.PasswordResetConfirmRequest(source.getToken(), source.getNewPassword());
    }

    static interview.coach.generated.model.VerificationResponse toGenerated(AuthDtos.VerificationResponse source) {
        return new interview.coach.generated.model.VerificationResponse()
                .message(source.message())
                .email(source.email())
                .code(source.code())
                .auth(source.auth() == null ? null : toGenerated(source.auth()));
    }

    static interview.coach.generated.model.AuthResponse toGenerated(AuthDtos.AuthResponse source) {
        return new interview.coach.generated.model.AuthResponse(
                source.accessToken(),
                source.refreshToken(),
                source.tokenType(),
                source.expiresIn(),
                toGenerated(source.user())
        );
    }

    static interview.coach.generated.model.PasswordResetResponse toGenerated(AuthDtos.PasswordResetResponse source) {
        return new interview.coach.generated.model.PasswordResetResponse()
                .message(source.message())
                .resetToken(source.resetToken());
    }

    static UserDtos.UpdateUserRequest toInternal(interview.coach.generated.model.UpdateUserRequest source) {
        return new UserDtos.UpdateUserRequest(
                source.getFirstName(),
                source.getLastName(),
                source.getPreferredDirection() == null ? null : DomainEnums.InterviewDirection.valueOf(source.getPreferredDirection().name()),
                source.getPreferredLevel() == null ? null : DomainEnums.InterviewLevel.valueOf(source.getPreferredLevel().name()),
                source.getPreferredLanguage(),
                source.getInterfaceLanguage(),
                source.getTheme()
        );
    }

    static interview.coach.generated.model.UserResponse toGenerated(UserDtos.UserResponse source) {
        return new interview.coach.generated.model.UserResponse(
                source.id(),
                source.email(),
                interview.coach.generated.model.UserStatus.valueOf(source.status().name()),
                source.role()
        )
                .firstName(source.firstName())
                .lastName(source.lastName())
                .preference(source.preference() == null ? null : toGenerated(source.preference()));
    }

    static interview.coach.generated.model.PreferenceResponse toGenerated(UserDtos.PreferenceResponse source) {
        return new interview.coach.generated.model.PreferenceResponse()
                .preferredDirection(source.preferredDirection() == null ? null : interview.coach.generated.model.InterviewDirection.valueOf(source.preferredDirection().name()))
                .preferredLevel(source.preferredLevel() == null ? null : interview.coach.generated.model.InterviewLevel.valueOf(source.preferredLevel().name()))
                .preferredLanguage(source.preferredLanguage())
                .interfaceLanguage(source.interfaceLanguage())
                .theme(source.theme());
    }

    static interview.coach.generated.model.ProgressResponse toGenerated(ProgressDtos.ProgressResponse source) {
        return new interview.coach.generated.model.ProgressResponse()
                .totalSessions(source.totalSessions())
                .finishedSessions(source.finishedSessions())
                .averageScore(toDouble(source.averageScore()))
                .reportsReady(source.reportsReady());
    }

    static interview.coach.generated.model.PageProfileResponse toGenerated(PageDtos.PageResponse<ProfileDtos.ProfileResponse> source) {
        interview.coach.generated.model.PageProfileResponse page = new interview.coach.generated.model.PageProfileResponse();
        page.setItems(source.items().stream().map(GeneratedApiSupport::toGenerated).toList());
        page.setPage(source.page());
        page.setSize(source.size());
        page.setTotalItems(source.totalItems());
        page.setTotalPages(source.totalPages());
        page.setHasNext(source.hasNext());
        return page;
    }

    static interview.coach.generated.model.ProfileResponse toGenerated(ProfileDtos.ProfileResponse source) {
        interview.coach.generated.model.ProfileResponse response = new interview.coach.generated.model.ProfileResponse();
        response.setId(source.id());
        response.setTitle(source.title());
        response.setDescription(source.description());
        response.setDirection(interview.coach.generated.model.InterviewDirection.valueOf(source.direction().name()));
        response.setLevel(interview.coach.generated.model.InterviewLevel.valueOf(source.level().name()));
        response.setStatus(interview.coach.generated.model.ProfileStatus.valueOf(source.status().name()));
        response.setTags(source.tags());
        response.setQuestions(source.questions().stream().map(GeneratedApiSupport::toGenerated).toList());
        response.setPublishedAt(toOffsetDateTime(source.publishedAt()));
        return response;
    }

    static interview.coach.generated.model.ProfileQuestionResponse toGenerated(ProfileDtos.ProfileQuestionResponse source) {
        return new interview.coach.generated.model.ProfileQuestionResponse()
                .id(source.id())
                .questionId(source.questionId())
                .questionText(source.questionText())
                .questionType(source.questionType())
                .orderIndex(source.orderIndex())
                .required(source.required());
    }

    static ProfileDtos.ProfileRequest toInternal(interview.coach.generated.model.ProfileRequest source) {
        return new ProfileDtos.ProfileRequest(
                source.getTitle(),
                source.getDescription(),
                DomainEnums.InterviewDirection.valueOf(source.getDirection().name()),
                DomainEnums.InterviewLevel.valueOf(source.getLevel().name()),
                source.getTags()
        );
    }

    static ProfileDtos.ProfileQuestionRequest toInternal(interview.coach.generated.model.ProfileQuestionRequest source) {
        return new ProfileDtos.ProfileQuestionRequest(source.getQuestionId(), source.getOrderIndex(), source.getRequired());
    }

    static AdminUserDtos.AdminUserUpdateRequest toInternal(interview.coach.generated.model.AdminUserUpdateRequest source) {
        return new AdminUserDtos.AdminUserUpdateRequest(
                source.getEmail(),
                source.getFirstName(),
                source.getLastName(),
                source.getStatus() == null ? null : DomainEnums.UserStatus.valueOf(source.getStatus().name()),
                source.getRoleCode(),
                source.getPreferredDirection() == null ? null : DomainEnums.InterviewDirection.valueOf(source.getPreferredDirection().name()),
                source.getPreferredLevel() == null ? null : DomainEnums.InterviewLevel.valueOf(source.getPreferredLevel().name()),
                source.getPreferredLanguage(),
                source.getInterfaceLanguage(),
                source.getTheme()
        );
    }

    static interview.coach.generated.model.AdminUserResponse toGenerated(AdminUserDtos.AdminUserResponse source) {
        interview.coach.generated.model.AdminUserResponse response = new interview.coach.generated.model.AdminUserResponse();
        response.setId(source.id());
        response.setEmail(source.email());
        response.setFirstName(source.firstName());
        response.setLastName(source.lastName());
        response.setStatus(interview.coach.generated.model.UserStatus.valueOf(source.status().name()));
        response.setRoleCode(source.roleCode());
        response.setPreference(source.preference() == null ? null : toGenerated(source.preference()));
        return response;
    }

    static QuestionDtos.QuestionRequest toInternal(interview.coach.generated.model.QuestionRequest source) {
        return new QuestionDtos.QuestionRequest(
                source.getText(),
                DomainEnums.QuestionType.valueOf(source.getQuestionType().name()),
                source.getDifficulty() == null ? null : DomainEnums.InterviewLevel.valueOf(source.getDifficulty().name()),
                source.getDirection() == null ? null : DomainEnums.InterviewDirection.valueOf(source.getDirection().name()),
                source.getStatus() == null ? null : DomainEnums.QuestionStatus.valueOf(source.getStatus().name())
        );
    }

    static interview.coach.generated.model.QuestionResponse toGenerated(QuestionDtos.QuestionResponse source) {
        interview.coach.generated.model.QuestionResponse response = new interview.coach.generated.model.QuestionResponse();
        response.setId(source.id());
        response.setText(source.text());
        response.setQuestionType(interview.coach.generated.model.QuestionType.valueOf(source.questionType().name()));
        response.setDifficulty(source.difficulty() == null ? null : interview.coach.generated.model.InterviewLevel.valueOf(source.difficulty().name()));
        response.setDirection(source.direction() == null ? null : interview.coach.generated.model.InterviewDirection.valueOf(source.direction().name()));
        response.setStatus(interview.coach.generated.model.QuestionStatus.valueOf(source.status().name()));
        response.setCreatedBy(source.createdBy());
        response.setCreatedAt(toOffsetDateTime(source.createdAt()));
        response.setUpdatedAt(toOffsetDateTime(source.updatedAt()));
        return response;
    }

    static List<interview.coach.generated.model.QuestionResponse> toGeneratedQuestions(List<QuestionDtos.QuestionResponse> source) {
        return source.stream().map(GeneratedApiSupport::toGenerated).toList();
    }

    static LocalDateTime toLocalDateTime(OffsetDateTime source) {
        return source == null ? null : source.toLocalDateTime();
    }

    static OffsetDateTime toOffsetDateTime(LocalDateTime source) {
        return source == null ? null : source.atOffset(ZoneOffset.UTC);
    }

    static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
