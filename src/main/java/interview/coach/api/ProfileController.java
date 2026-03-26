package interview.coach.api;

import interview.coach.generated.api.ProfilesApi;
import interview.coach.service.InterviewProfileService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController implements ProfilesApi {

    private final InterviewProfileService interviewProfileService;

    public ProfileController(InterviewProfileService interviewProfileService) {
        this.interviewProfileService = interviewProfileService;
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.PageProfileResponse> profilesGet(
            interview.coach.generated.model.InterviewDirection direction,
            interview.coach.generated.model.InterviewLevel level,
            String query,
            String tag,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(
                interviewProfileService.getCatalog(
                        direction == null ? null : interview.coach.domain.DomainEnums.InterviewDirection.valueOf(direction.name()),
                        level == null ? null : interview.coach.domain.DomainEnums.InterviewLevel.valueOf(level.name()),
                        query,
                        tag,
                        page,
                        size
                )
        ));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.ProfileResponse> profilesProfileIdGet(UUID profileId) {
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(interviewProfileService.getPublishedProfile(profileId)));
    }
}
