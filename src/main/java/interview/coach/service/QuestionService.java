package interview.coach.service;

import interview.coach.api.dto.QuestionDtos.QuestionPageResponse;
import interview.coach.api.dto.QuestionDtos.QuestionRequest;
import interview.coach.api.dto.QuestionDtos.QuestionResponse;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import interview.coach.domain.entity.Question;
import interview.coach.domain.entity.ProfileQuestion;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.QuestionRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final CatalogReferenceService catalogReferenceService;

    public QuestionService(
            QuestionRepository questionRepository,
            UserService userService,
            CatalogReferenceService catalogReferenceService
    ) {
        this.questionRepository = questionRepository;
        this.userService = userService;
        this.catalogReferenceService = catalogReferenceService;
    }

    @Transactional(readOnly = true)
    public QuestionPageResponse getAll(
            String query,
            String search,
            String direction,
            String difficulty,
            QuestionType questionType,
            QuestionStatus status,
            UUID excludeProfileId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        String effectiveQuery = firstNonBlank(query, search);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(parseSortDirection(sortDir), normalizeSortBy(sortBy))
        );
        Specification<Question> specification = null;
        specification = andIfPresent(specification, matchesQuery(effectiveQuery));
        specification = andIfPresent(specification, hasDirection(direction));
        specification = andIfPresent(specification, hasDifficulty(difficulty));
        specification = andIfPresent(specification, hasQuestionType(questionType));
        specification = andIfPresent(specification, hasStatus(status));
        specification = andIfPresent(specification, excludeProfileQuestions(excludeProfileId));

        return QuestionPageResponse.from(questionRepository.findAll(specification, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
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
        question.setDifficulty(request.difficulty() == null ? null : catalogReferenceService.requireLevel(request.difficulty()));
        question.setDirection(request.direction() == null ? null : catalogReferenceService.requireDirection(request.direction()));
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
        question.setDifficulty(request.difficulty() == null ? null : catalogReferenceService.requireLevel(request.difficulty()));
        question.setDirection(request.direction() == null ? null : catalogReferenceService.requireDirection(request.direction()));
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

    private Specification<Question> matchesQuery(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return null;
        }
        String pattern = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("text")), pattern);
    }

    private Specification<Question> hasDirection(String direction) {
        if (direction == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.upper(root.get("direction").get("code")), direction.trim().toUpperCase(Locale.ROOT));
    }

    private Specification<Question> hasDifficulty(String difficulty) {
        if (difficulty == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.upper(root.get("difficulty").get("code")), difficulty.trim().toUpperCase(Locale.ROOT));
    }

    private Specification<Question> hasQuestionType(QuestionType questionType) {
        if (questionType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("questionType"), questionType);
    }

    private Specification<Question> hasStatus(QuestionStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<Question> excludeProfileQuestions(UUID profileId) {
        if (profileId == null) {
            return null;
        }
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            var profileQuestionRoot = subquery.from(ProfileQuestion.class);
            subquery.select(profileQuestionRoot.get("question").get("id"))
                    .where(cb.equal(profileQuestionRoot.get("profile").get("id"), profileId));
            return cb.not(root.get("id").in(subquery));
        };
    }

    private Specification<Question> andIfPresent(Specification<Question> base, Specification<Question> addition) {
        return addition == null ? base : (base == null ? Specification.where(addition) : base.and(addition));
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 50);
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "updatedAt";
        }
        return switch (sortBy) {
            case "updatedAt", "createdAt", "text" -> sortBy;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported sortBy value");
        };
    }

    private Sort.Direction parseSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.DESC;
        }
        return switch (sortDir.toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported sortDir value");
        };
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getQuestionType(),
                question.getDifficulty() == null ? null : question.getDifficulty().getCode(),
                question.getDirection() == null ? null : question.getDirection().getCode(),
                question.getStatus(),
                question.getCreatedBy().getId(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
