package interview.coach.api;

import interview.coach.api.dto.ProgressDtos.ProgressResponse;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ResponseEntity<ProgressResponse> progress(@AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(progressService.getProgress(principal));
    }
}
