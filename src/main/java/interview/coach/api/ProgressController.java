package interview.coach.api;

import interview.coach.generated.api.ProgressApi;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProgressController implements ProgressApi {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.ProgressResponse> progressGet() {
        AppUserPrincipal principal = GeneratedApiSupport.currentPrincipal();
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(progressService.getProgress(principal)));
    }
}
