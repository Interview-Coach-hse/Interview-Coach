package interview.coach.api;

import interview.coach.api.dto.QuestionDtos.QuestionRequest;
import interview.coach.api.dto.QuestionDtos.QuestionResponse;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.QuestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final QuestionService questionService;

    public AdminQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAll() {
        return ResponseEntity.ok(questionService.getAll());
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable UUID questionId) {
        return ResponseEntity.ok(questionService.getById(questionId));
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.create(principal, request));
    }

    @PatchMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> update(
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.update(questionId, request));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID questionId) {
        questionService.delete(questionId);
        return ResponseEntity.noContent().build();
    }
}
