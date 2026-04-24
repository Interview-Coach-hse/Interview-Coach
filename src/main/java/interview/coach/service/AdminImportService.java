package interview.coach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.coach.api.dto.ImportDtos.ImportProfilePayload;
import interview.coach.api.dto.ImportDtos.ImportQuestionPayload;
import interview.coach.api.dto.ImportDtos.ImportRequest;
import interview.coach.api.dto.ImportDtos.ImportResponse;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.DomainEnums.QuestionStatus;
import interview.coach.domain.DomainEnums.QuestionType;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.ProfileQuestion;
import interview.coach.domain.entity.ProfileTag;
import interview.coach.domain.entity.Question;
import interview.coach.domain.entity.Tag;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.InterviewProfileRepository;
import interview.coach.repository.ProfileQuestionRepository;
import interview.coach.repository.ProfileTagRepository;
import interview.coach.repository.QuestionRepository;
import interview.coach.repository.TagRepository;
import interview.coach.security.AppUserPrincipal;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminImportService {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final QuestionRepository questionRepository;
    private final InterviewProfileRepository interviewProfileRepository;
    private final ProfileQuestionRepository profileQuestionRepository;
    private final ProfileTagRepository profileTagRepository;
    private final TagRepository tagRepository;

    public AdminImportService(
            ObjectMapper objectMapper,
            UserService userService,
            QuestionRepository questionRepository,
            InterviewProfileRepository interviewProfileRepository,
            ProfileQuestionRepository profileQuestionRepository,
            ProfileTagRepository profileTagRepository,
            TagRepository tagRepository
    ) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.questionRepository = questionRepository;
        this.interviewProfileRepository = interviewProfileRepository;
        this.profileQuestionRepository = profileQuestionRepository;
        this.profileTagRepository = profileTagRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public ImportResponse importJson(AppUserPrincipal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Import file is required");
        }

        User admin = userService.getCurrentUser(principal);
        ImportRequest request = parseRequest(file);
        List<ImportQuestionPayload> questions = requireQuestions(request.questions());

        if (request.profile() == null) {
            ImportStats stats = importQuestionsOnly(admin, questions);
            return new ImportResponse("QUESTIONS", null, null, questions.size(), stats.createdQuestions, stats.reusedQuestions, 0);
        }

        InterviewProfile profile = createProfile(admin, request.profile());
        ImportStats stats = importProfileQuestions(admin, profile, questions);
        return new ImportResponse(
                "PROFILE",
                profile.getId(),
                profile.getTitle(),
                questions.size(),
                stats.createdQuestions,
                stats.reusedQuestions,
                stats.linkedQuestions
        );
    }

    private ImportRequest parseRequest(MultipartFile file) {
        try {
            JsonNode root = objectMapper.readTree(file.getInputStream());
            if (root == null || root.isNull()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Import JSON is empty");
            }
            if (root.isArray()) {
                List<ImportQuestionPayload> questions = objectMapper.readerForListOf(ImportQuestionPayload.class).readValue(root);
                return new ImportRequest(null, questions);
            }
            if (!root.isObject()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Import JSON must be an object or an array of questions");
            }
            return objectMapper.treeToValue(root, ImportRequest.class);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Import JSON has invalid structure");
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Failed to read import file");
        }
    }

    private List<ImportQuestionPayload> requireQuestions(List<ImportQuestionPayload> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Import must contain at least one question");
        }
        return questions;
    }

    private ImportStats importQuestionsOnly(User admin, List<ImportQuestionPayload> questions) {
        ImportStats stats = new ImportStats();
        for (ImportQuestionPayload payload : questions) {
            QuestionResult result = resolveQuestion(admin, payload, null);
            stats.record(result.created);
        }
        return stats;
    }

    private ImportStats importProfileQuestions(User admin, InterviewProfile profile, List<ImportQuestionPayload> questions) {
        ImportStats stats = new ImportStats();
        Set<Integer> usedOrderIndexes = new HashSet<>();
        Set<java.util.UUID> linkedQuestionIds = new HashSet<>();

        for (int index = 0; index < questions.size(); index++) {
            ImportQuestionPayload payload = questions.get(index);
            QuestionResult result = resolveQuestion(admin, payload, profile);
            int orderIndex = payload.orderIndex() == null ? index : payload.orderIndex();
            if (!usedOrderIndexes.add(orderIndex)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate orderIndex in imported profile questions");
            }
            if (!linkedQuestionIds.add(result.question.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate question in imported profile questions");
            }

            ProfileQuestion profileQuestion = new ProfileQuestion();
            profileQuestion.setProfile(profile);
            profileQuestion.setQuestion(result.question);
            profileQuestion.setOrderIndex(orderIndex);
            profileQuestion.setRequired(payload.required() == null || payload.required());
            profileQuestion.setCreatedAt(LocalDateTime.now());
            profileQuestionRepository.save(profileQuestion);

            stats.record(result.created);
            stats.linkedQuestions++;
        }

        return stats;
    }

    private InterviewProfile createProfile(User admin, ImportProfilePayload payload) {
        if (payload == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile payload is required");
        }
        if (payload.title() == null || payload.title().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile title is required");
        }
        if (payload.description() == null || payload.description().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile description is required");
        }
        if (payload.direction() == null || payload.level() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Profile direction and level are required");
        }

        LocalDateTime now = LocalDateTime.now();
        InterviewProfile profile = new InterviewProfile();
        profile.setTitle(payload.title().trim());
        profile.setDescription(payload.description().trim());
        profile.setDirection(payload.direction());
        profile.setLevel(payload.level());
        profile.setStatus(ProfileStatus.DRAFT);
        profile.setCreatedBy(admin);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        interviewProfileRepository.save(profile);
        syncTags(profile, payload.tags());
        return profile;
    }

    private void syncTags(InterviewProfile profile, List<String> requestedTags) {
        if (requestedTags == null || requestedTags.isEmpty()) {
            return;
        }
        Set<String> normalizedTags = requestedTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        for (String tagName : normalizedTags) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(tagName);
                created.setCreatedAt(LocalDateTime.now());
                return tagRepository.save(created);
            });

            ProfileTag profileTag = new ProfileTag();
            profileTag.setProfile(profile);
            profileTag.setTag(tag);
            profileTagRepository.save(profileTag);
        }
    }

    private QuestionResult resolveQuestion(User admin, ImportQuestionPayload payload, InterviewProfile profile) {
        String normalizedText = normalizeText(payload.text());
        Question existing = questionRepository.findFirstByTextIgnoreCase(normalizedText).orElse(null);
        if (existing != null) {
            return new QuestionResult(existing, false);
        }

        LocalDateTime now = LocalDateTime.now();
        Question question = new Question();
        question.setText(normalizedText);
        question.setQuestionType(payload.questionType() == null ? QuestionType.TECHNICAL : payload.questionType());
        question.setDifficulty(payload.difficulty() != null ? payload.difficulty() : profile == null ? null : profile.getLevel());
        question.setDirection(payload.direction() != null ? payload.direction() : profile == null ? null : profile.getDirection());
        question.setStatus(payload.status() == null ? QuestionStatus.ACTIVE : payload.status());
        question.setCreatedBy(admin);
        question.setCreatedAt(now);
        question.setUpdatedAt(now);
        return new QuestionResult(questionRepository.save(question), true);
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Each imported question must have text");
        }
        return text.trim();
    }

    private static final class QuestionResult {
        private final Question question;
        private final boolean created;

        private QuestionResult(Question question, boolean created) {
            this.question = question;
            this.created = created;
        }
    }

    private static final class ImportStats {
        private int createdQuestions;
        private int reusedQuestions;
        private int linkedQuestions;

        private void record(boolean created) {
            if (created) {
                createdQuestions++;
                return;
            }
            reusedQuestions++;
        }
    }
}
