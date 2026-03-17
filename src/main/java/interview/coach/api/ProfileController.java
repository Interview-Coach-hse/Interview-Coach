package interview.coach.api;

import interview.coach.api.dto.ProfileDtos.ProfileResponse;
import interview.coach.api.dto.PageDtos.PageResponse;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.service.InterviewProfileService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final InterviewProfileService interviewProfileService;

    public ProfileController(InterviewProfileService interviewProfileService) {
        this.interviewProfileService = interviewProfileService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProfileResponse>> catalog(
            @RequestParam(required = false) InterviewDirection direction,
            @RequestParam(required = false) InterviewLevel level,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(interviewProfileService.getCatalog(direction, level, query, tag, page, size));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ProfileResponse> byId(@PathVariable UUID profileId) {
        return ResponseEntity.ok(interviewProfileService.getPublishedProfile(profileId));
    }
}
