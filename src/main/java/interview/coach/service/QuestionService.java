package interview.coach.service;

import interview.coach.api.dto.QuestionDtos.QuestionRequest;
import interview.coach.api.dto.QuestionDtos.QuestionResponse;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.entity.Question;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.QuestionRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserService userService;

    public QuestionService(QuestionRepository questionRepository, UserService userService) {
        this.questionRepository = questionRepository;
        this.userService = userService;
    }

    public List<QuestionResponse> getAll() {
        return questionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public QuestionResponse getById(UUID questionId) {
        return toResponse(requireQuestion(questionId));
    }

    @Transactional
    public QuestionResponse create(AppUserPrincipal principal, QuestionRequest request) {
        User creator = userService.getCurrentUser(principal);
        LocalDateTime now = LocalDateTime.now();

        Question question = new Question();
        question.setText(request.text());
        question.setQuestionType(request.questionType());
        question.setDifficulty(request.difficulty());
        question.setDirection(request.direction());
        question.setStatus(request.status() == null ? QuestionStatus.ACTIVE : request.status());
        question.setCreatedBy(creator);
        question.setCreatedAt(now);
        question.setUpdatedAt(now);
        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse update(UUID questionId, QuestionRequest request) {
        Question question = requireQuestion(questionId);
        question.setText(request.text());
        question.setQuestionType(request.questionType());
        question.setDifficulty(request.difficulty());
        question.setDirection(request.direction());
        question.setStatus(request.status() == null ? question.getStatus() : request.status());
        question.setUpdatedAt(LocalDateTime.now());
        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public void delete(UUID questionId) {
        Question question = requireQuestion(questionId);
        questionRepository.delete(question);
    }

    private Question requireQuestion(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getQuestionType(),
                question.getDifficulty(),
                question.getDirection(),
                question.getStatus(),
                question.getCreatedBy().getId(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
