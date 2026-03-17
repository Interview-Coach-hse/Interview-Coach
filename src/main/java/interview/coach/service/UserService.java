package interview.coach.service;

import interview.coach.api.dto.UserDtos.PreferenceResponse;
import interview.coach.api.dto.UserDtos.UpdateUserRequest;
import interview.coach.api.dto.UserDtos.UserResponse;
import interview.coach.domain.entity.User;
import interview.coach.domain.entity.UserPreference;
import interview.coach.exception.ApiException;
import interview.coach.repository.UserPreferenceRepository;
import interview.coach.repository.UserRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserService(UserRepository userRepository, UserPreferenceRepository userPreferenceRepository) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public User getCurrentUser(AppUserPrincipal principal) {
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }

    public UserResponse getCurrentUserProfile(AppUserPrincipal principal) {
        return toResponse(getCurrentUser(principal));
    }

    @Transactional
    public UserResponse updateCurrentUser(AppUserPrincipal principal, UpdateUserRequest request) {
        User user = getCurrentUser(principal);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setUpdatedAt(LocalDateTime.now());

        UserPreference preference = userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPreference created = new UserPreference();
                    created.setUser(user);
                    return created;
                });
        preference.setPreferredDirection(request.preferredDirection());
        preference.setPreferredLevel(request.preferredLevel());
        preference.setPreferredLanguage(request.preferredLanguage());
        preference.setInterfaceLanguage(request.interfaceLanguage());
        preference.setTheme(request.theme());
        preference.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        userPreferenceRepository.save(preference);
        return toResponse(user);
    }

    public UserResponse toResponse(User user) {
        UserPreference preference = user.getPreference() != null
                ? user.getPreference()
                : userPreferenceRepository.findByUserId(user.getId()).orElse(null);

        PreferenceResponse preferenceResponse = preference == null ? null : new PreferenceResponse(
                preference.getPreferredDirection(),
                preference.getPreferredLevel(),
                preference.getPreferredLanguage(),
                preference.getInterfaceLanguage(),
                preference.getTheme()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getRole().getCode(),
                preferenceResponse
        );
    }
}
