package interview.coach.api;

import interview.coach.api.dto.ProfileDtos.ProfileRequest;
import interview.coach.api.dto.ProfileDtos.ProfileResponse;
import interview.coach.api.dto.PageDtos.PageResponse;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.InterviewProfileService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/profiles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    private final InterviewProfileService interviewProfileService;

    public AdminProfileController(InterviewProfileService interviewProfileService) {
        this.interviewProfileService = interviewProfileService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProfileResponse>> list(
            @RequestParam(required = false) ProfileStatus status,
            @RequestParam(required = false) InterviewDirection direction,
            @RequestParam(required = false) InterviewLevel level,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(interviewProfileService.getAdminCatalog(status, direction, level, query, tag, page, size));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> getById(@PathVariable UUID profileId) {
        return ResponseEntity.ok(interviewProfileService.getProfile(profileId));
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody ProfileRequest request
    ) {
        return ResponseEntity.ok(interviewProfileService.create(principal, request));
    }

    @PatchMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> update(@PathVariable UUID profileId, @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(interviewProfileService.update(profileId, request));
    }

    @PostMapping("/{profileId}/publish")
    public ResponseEntity<ProfileResponse> publish(@PathVariable UUID profileId) {
        return ResponseEntity.ok(interviewProfileService.publish(profileId));
    }

    @PostMapping("/{profileId}/archive")
    public ResponseEntity<ProfileResponse> archive(@PathVariable UUID profileId) {
        return ResponseEntity.ok(interviewProfileService.archive(profileId));
    }
}
