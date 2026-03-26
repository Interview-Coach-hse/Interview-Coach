package interview.coach.api;

import interview.coach.generated.api.AdminQuestionsApi;
import interview.coach.security.AppUserPrincipal;
import interview.coach.service.QuestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController implements AdminQuestionsApi {

    private final QuestionService questionService;

    public AdminQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public ResponseEntity<List<interview.coach.generated.model.QuestionResponse>> adminQuestionsGet() {
        return ResponseEntity.ok(GeneratedApiSupport.toGeneratedQuestions(questionService.getAll()));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.QuestionResponse> adminQuestionsQuestionIdGet(UUID questionId) {
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(questionService.getById(questionId)));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.QuestionResponse> adminQuestionsPost(
            @Valid @RequestBody interview.coach.generated.model.QuestionRequest request
    ) {
        AppUserPrincipal principal = GeneratedApiSupport.currentPrincipal();
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(
                questionService.create(principal, GeneratedApiSupport.toInternal(request))
        ));
    }

    @Override
    public ResponseEntity<interview.coach.generated.model.QuestionResponse> adminQuestionsQuestionIdPatch(
            UUID questionId,
            @Valid @RequestBody interview.coach.generated.model.QuestionRequest request
    ) {
        return ResponseEntity.ok(GeneratedApiSupport.toGenerated(
                questionService.update(questionId, GeneratedApiSupport.toInternal(request))
        ));
    }

    @Override
    public ResponseEntity<Void> adminQuestionsQuestionIdDelete(UUID questionId) {
        questionService.delete(questionId);
        return ResponseEntity.noContent().build();
    }
}
