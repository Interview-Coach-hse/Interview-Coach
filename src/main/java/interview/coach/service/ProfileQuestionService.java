package interview.coach.service;

import interview.coach.api.dto.ProfileDtos.ProfileQuestionRequest;
import interview.coach.api.dto.ProfileDtos.ProfileQuestionResponse;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.ProfileQuestion;
import interview.coach.domain.entity.Question;
import interview.coach.exception.ApiException;
import interview.coach.repository.ProfileQuestionRepository;
import interview.coach.repository.QuestionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileQuestionService {

    private final ProfileQuestionRepository profileQuestionRepository;
    private final QuestionRepository questionRepository;
    private final InterviewProfileService interviewProfileService;

    public ProfileQuestionService(
            ProfileQuestionRepository profileQuestionRepository,
            QuestionRepository questionRepository,
            InterviewProfileService interviewProfileService
    ) {
        this.profileQuestionRepository = profileQuestionRepository;
        this.questionRepository = questionRepository;
        this.interviewProfileService = interviewProfileService;
    }

    public List<ProfileQuestionResponse> getProfileQuestions(UUID profileId) {
        interviewProfileService.requireExisting(profileId);
        return profileQuestionRepository.findByProfileIdOrderByOrderIndexAsc(profileId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProfileQuestionResponse addQuestion(UUID profileId, ProfileQuestionRequest request) {
        InterviewProfile profile = interviewProfileService.requireExisting(profileId);
        Question question = requireQuestion(request.questionId());
        validateOrder(profileId, request.orderIndex(), null);
        if (profileQuestionRepository.existsByProfileIdAndQuestionId(profileId, question.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Question is already linked to profile");
        }

        ProfileQuestion profileQuestion = new ProfileQuestion();
        profileQuestion.setProfile(profile);
        profileQuestion.setQuestion(question);
        profileQuestion.setOrderIndex(request.orderIndex());
        profileQuestion.setRequired(request.required() == null || request.required());
        profileQuestion.setCreatedAt(LocalDateTime.now());
        return toResponse(profileQuestionRepository.save(profileQuestion));
    }

    @Transactional
    public ProfileQuestionResponse updateQuestion(UUID profileId, UUID linkId, ProfileQuestionRequest request) {
        ProfileQuestion profileQuestion = profileQuestionRepository.findByIdAndProfileId(linkId, profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile question link not found"));
        Question question = requireQuestion(request.questionId());
        validateOrder(profileId, request.orderIndex(), profileQuestion.getId());

        profileQuestion.setQuestion(question);
        profileQuestion.setOrderIndex(request.orderIndex());
        profileQuestion.setRequired(request.required() == null || request.required());
        return toResponse(profileQuestionRepository.save(profileQuestion));
    }

    @Transactional
    public void deleteQuestion(UUID profileId, UUID linkId) {
        ProfileQuestion profileQuestion = profileQuestionRepository.findByIdAndProfileId(linkId, profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile question link not found"));
        profileQuestionRepository.delete(profileQuestion);
    }

    private void validateOrder(UUID profileId, int orderIndex, UUID currentId) {
        boolean exists = currentId == null
                ? profileQuestionRepository.existsByProfileIdAndOrderIndex(profileId, orderIndex)
                : profileQuestionRepository.existsByProfileIdAndOrderIndexAndIdNot(profileId, orderIndex, currentId);
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "Order index is already used in this profile");
        }
    }

    private Question requireQuestion(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private ProfileQuestionResponse toResponse(ProfileQuestion profileQuestion) {
        return new ProfileQuestionResponse(
                profileQuestion.getId(),
                profileQuestion.getQuestion().getId(),
                profileQuestion.getQuestion().getText(),
                profileQuestion.getQuestion().getQuestionType().name(),
                profileQuestion.getOrderIndex(),
                profileQuestion.isRequired()
        );
    }
}
