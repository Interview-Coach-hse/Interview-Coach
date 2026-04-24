package interview.coach.api;

import interview.coach.api.dto.ImportDtos.ImportResponse;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.AdminImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/import")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImportController {

    private final AdminImportService adminImportService;

    public AdminImportController(AdminImportService adminImportService) {
        this.adminImportService = adminImportService;
    }

    @PostMapping
    public ResponseEntity<ImportResponse> importJson(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(adminImportService.importJson(principal, file));
    }
}
