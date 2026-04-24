package interview.coach.api;

import interview.coach.api.dto.CatalogDtos.CatalogItemRequest;
import interview.coach.api.dto.CatalogDtos.CatalogItemResponse;
import interview.coach.service.CatalogReferenceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final CatalogReferenceService catalogReferenceService;

    public AdminCatalogController(CatalogReferenceService catalogReferenceService) {
        this.catalogReferenceService = catalogReferenceService;
    }

    @GetMapping("/directions")
    public ResponseEntity<List<CatalogItemResponse>> directions() {
        return ResponseEntity.ok(catalogReferenceService.getDirections());
    }

    @PostMapping("/directions")
    public ResponseEntity<CatalogItemResponse> createDirection(@Valid @RequestBody CatalogItemRequest request) {
        return ResponseEntity.ok(catalogReferenceService.createDirection(request));
    }

    @PatchMapping("/directions/{directionId}")
    public ResponseEntity<CatalogItemResponse> updateDirection(
            @PathVariable UUID directionId,
            @Valid @RequestBody CatalogItemRequest request
    ) {
        return ResponseEntity.ok(catalogReferenceService.updateDirection(directionId, request));
    }

    @DeleteMapping("/directions/{directionId}")
    public ResponseEntity<Void> deleteDirection(@PathVariable UUID directionId) {
        catalogReferenceService.deleteDirection(directionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/levels")
    public ResponseEntity<List<CatalogItemResponse>> levels() {
        return ResponseEntity.ok(catalogReferenceService.getLevels());
    }

    @PostMapping("/levels")
    public ResponseEntity<CatalogItemResponse> createLevel(@Valid @RequestBody CatalogItemRequest request) {
        return ResponseEntity.ok(catalogReferenceService.createLevel(request));
    }

    @PatchMapping("/levels/{levelId}")
    public ResponseEntity<CatalogItemResponse> updateLevel(
            @PathVariable UUID levelId,
            @Valid @RequestBody CatalogItemRequest request
    ) {
        return ResponseEntity.ok(catalogReferenceService.updateLevel(levelId, request));
    }

    @DeleteMapping("/levels/{levelId}")
    public ResponseEntity<Void> deleteLevel(@PathVariable UUID levelId) {
        catalogReferenceService.deleteLevel(levelId);
        return ResponseEntity.noContent().build();
    }
}
