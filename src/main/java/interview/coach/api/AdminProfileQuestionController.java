package interview.coach.api;

import interview.coach.api.dto.ProfileDtos.ProfileQuestionRequest;
import interview.coach.api.dto.ProfileDtos.ProfileQuestionResponse;
import interview.coach.service.ProfileQuestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/profiles/{profileId}/questions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileQuestionController {

    private final ProfileQuestionService profileQuestionService;

    public AdminProfileQuestionController(ProfileQuestionService profileQuestionService) {
        this.profileQuestionService = profileQuestionService;
    }

    @GetMapping
    public ResponseEntity<List<ProfileQuestionResponse>> list(@PathVariable UUID profileId) {
        return ResponseEntity.ok(profileQuestionService.getProfileQuestions(profileId));
    }

    @PostMapping
    public ResponseEntity<ProfileQuestionResponse> create(
            @PathVariable UUID profileId,
            @Valid @RequestBody ProfileQuestionRequest request
    ) {
        return ResponseEntity.ok(profileQuestionService.addQuestion(profileId, request));
    }

    @PatchMapping("/{linkId}")
    public ResponseEntity<ProfileQuestionResponse> update(
            @PathVariable UUID profileId,
            @PathVariable UUID linkId,
            @Valid @RequestBody ProfileQuestionRequest request
    ) {
        return ResponseEntity.ok(profileQuestionService.updateQuestion(profileId, linkId, request));
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> delete(@PathVariable UUID profileId, @PathVariable UUID linkId) {
        profileQuestionService.deleteQuestion(profileId, linkId);
        return ResponseEntity.noContent().build();
    }
}
