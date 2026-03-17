package interview.coach.service;

import interview.coach.api.dto.AdminUserDtos.AdminUserResponse;
import interview.coach.api.dto.AdminUserDtos.AdminUserUpdateRequest;
import interview.coach.api.dto.UserDtos.PreferenceResponse;
import interview.coach.domain.entity.Role;
import interview.coach.domain.entity.User;
import interview.coach.domain.entity.UserPreference;
import interview.coach.exception.ApiException;
import interview.coach.repository.RoleRepository;
import interview.coach.repository.UserPreferenceRepository;
import interview.coach.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.roleRepository = roleRepository;
    }

    public List<AdminUserResponse> getUsers(String email, String roleCode) {
        return userRepository.findAll().stream()
                .filter(user -> matchesEmail(user, email))
                .filter(user -> matchesRole(user, roleCode))
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse getUser(UUID userId) {
        return toResponse(requireUser(userId));
    }

    @Transactional
    public AdminUserResponse updateUser(UUID userId, AdminUserUpdateRequest request) {
        User user = requireUser(userId);
        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
            if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new ApiException(HttpStatus.CONFLICT, "User with this email already exists");
            }
            user.setEmail(normalizedEmail);
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        if (request.roleCode() != null && !request.roleCode().isBlank()) {
            Role role = roleRepository.findByCode(request.roleCode().trim().toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role not found"));
            user.setRole(role);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if (hasPreferenceUpdate(request)) {
            UserPreference preference = userPreferenceRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        UserPreference created = new UserPreference();
                        created.setUser(user);
                        return created;
                    });
            if (request.preferredDirection() != null) {
                preference.setPreferredDirection(request.preferredDirection());
            }
            if (request.preferredLevel() != null) {
                preference.setPreferredLevel(request.preferredLevel());
            }
            if (request.preferredLanguage() != null) {
                preference.setPreferredLanguage(request.preferredLanguage());
            }
            if (request.interfaceLanguage() != null) {
                preference.setInterfaceLanguage(request.interfaceLanguage());
            }
            if (request.theme() != null) {
                preference.setTheme(request.theme());
            }
            preference.setUpdatedAt(LocalDateTime.now());
            userPreferenceRepository.save(preference);
        }

        return toResponse(userRepository.findById(userId).orElseThrow());
    }

    private boolean matchesEmail(User user, String email) {
        if (email == null || email.isBlank()) {
            return true;
        }
        return user.getEmail().toLowerCase(Locale.ROOT).contains(email.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesRole(User user, String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return true;
        }
        return user.getRole().getCode().equalsIgnoreCase(roleCode.trim());
    }

    private boolean hasPreferenceUpdate(AdminUserUpdateRequest request) {
        return request.preferredDirection() != null
                || request.preferredLevel() != null
                || request.preferredLanguage() != null
                || request.interfaceLanguage() != null
                || request.theme() != null;
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AdminUserResponse toResponse(User user) {
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
        return new AdminUserResponse(
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
