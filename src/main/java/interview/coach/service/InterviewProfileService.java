package interview.coach.service;

import interview.coach.api.dto.ProfileDtos.ProfileRequest;
import interview.coach.api.dto.ProfileDtos.ProfileResponse;
import interview.coach.api.dto.PageDtos.PageResponse;
import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.entity.InterviewProfile;
import interview.coach.domain.entity.ProfileTag;
import interview.coach.domain.entity.ProfileQuestion;
import interview.coach.domain.entity.Tag;
import interview.coach.domain.entity.User;
import interview.coach.exception.ApiException;
import interview.coach.repository.InterviewProfileRepository;
import interview.coach.repository.ProfileTagRepository;
import interview.coach.repository.TagRepository;
import interview.coach.security.AppUserPrincipal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewProfileService {

    private final InterviewProfileRepository interviewProfileRepository;
    private final ProfileTagRepository profileTagRepository;
    private final interview.coach.repository.ProfileQuestionRepository profileQuestionRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

    public InterviewProfileService(
            InterviewProfileRepository interviewProfileRepository,
            ProfileTagRepository profileTagRepository,
            interview.coach.repository.ProfileQuestionRepository profileQuestionRepository,
            TagRepository tagRepository,
            UserService userService
    ) {
        this.interviewProfileRepository = interviewProfileRepository;
        this.profileTagRepository = profileTagRepository;
        this.profileQuestionRepository = profileQuestionRepository;
        this.tagRepository = tagRepository;
        this.userService = userService;
    }

    public PageResponse<ProfileResponse> getCatalog(
            InterviewDirection direction,
            InterviewLevel level,
            String query,
            String tag,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Specification<InterviewProfile> specification = hasStatus(ProfileStatus.PUBLISHED);

        if (direction != null) {
            specification = specification.and(hasDirection(direction));
        }
        if (level != null) {
            specification = specification.and(hasLevel(level));
        }
        if (query != null && !query.isBlank()) {
            specification = specification.and(matchesQuery(query));
        }
        if (tag != null && !tag.isBlank()) {
            specification = specification.and(matchesTag(tag));
        }

        return PageResponse.from(interviewProfileRepository.findAll(specification, pageable).map(this::toResponse));
    }

    public ProfileResponse getPublishedProfile(UUID profileId) {
        InterviewProfile profile = interviewProfileRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Interview profile not found"));
        if (profile.getStatus() != ProfileStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Interview profile is not published");
        }
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse create(AppUserPrincipal principal, ProfileRequest request) {
        User author = userService.getCurrentUser(principal);
        LocalDateTime now = LocalDateTime.now();

        InterviewProfile profile = new InterviewProfile();
        profile.setTitle(request.title());
        profile.setDescription(request.description());
        profile.setDirection(request.direction());
        profile.setLevel(request.level());
        profile.setStatus(ProfileStatus.DRAFT);
        profile.setCreatedBy(author);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        interviewProfileRepository.save(profile);

        syncTags(profile, request.tags());
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse update(UUID profileId, ProfileRequest request) {
        InterviewProfile profile = requireProfile(profileId);
        profile.setTitle(request.title());
        profile.setDescription(request.description());
        profile.setDirection(request.direction());
        profile.setLevel(request.level());
        profile.setUpdatedAt(LocalDateTime.now());
        interviewProfileRepository.save(profile);
        syncTags(profile, request.tags());
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse publish(UUID profileId) {
        InterviewProfile profile = requireProfile(profileId);
        profile.setStatus(ProfileStatus.PUBLISHED);
        profile.setPublishedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        interviewProfileRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse archive(UUID profileId) {
        InterviewProfile profile = requireProfile(profileId);
        profile.setStatus(ProfileStatus.ARCHIVED);
        profile.setArchivedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        interviewProfileRepository.save(profile);
        return toResponse(profile);
    }

    public InterviewProfile requirePublished(UUID profileId) {
        InterviewProfile profile = requireProfile(profileId);
        if (profile.getStatus() != ProfileStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only published interview profiles can be used");
        }
        return profile;
    }

    public InterviewProfile requireExisting(UUID profileId) {
        return requireProfile(profileId);
    }

    private InterviewProfile requireProfile(UUID profileId) {
        return interviewProfileRepository.findById(profileId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Interview profile not found"));
    }

    private Specification<InterviewProfile> hasStatus(ProfileStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<InterviewProfile> hasDirection(InterviewDirection direction) {
        if (direction == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("direction"), direction);
    }

    private Specification<InterviewProfile> hasLevel(InterviewLevel level) {
        if (level == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("level"), level);
    }

    private Specification<InterviewProfile> matchesQuery(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return null;
        }
        String pattern = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    private Specification<InterviewProfile> matchesTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        String normalized = tag.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> {
            query.distinct(true);
            var subquery = query.subquery(UUID.class);
            var profileTagRoot = subquery.from(ProfileTag.class);
            var tagJoin = profileTagRoot.join("tag");
            subquery.select(profileTagRoot.get("profile").get("id"))
                    .where(
                            cb.equal(profileTagRoot.get("profile").get("id"), root.get("id")),
                            cb.equal(cb.lower(tagJoin.get("name")), normalized)
                    );
            return cb.exists(subquery);
        };
    }

    private void syncTags(InterviewProfile profile, List<String> requestedTags) {
        profileTagRepository.deleteByProfileId(profile.getId());
        if (requestedTags == null || requestedTags.isEmpty()) {
            return;
        }

        List<String> normalizedTags = requestedTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
        List<ProfileTag> profileTags = new ArrayList<>();
        for (String tagName : normalizedTags) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() -> {
                        Tag created = new Tag();
                        created.setName(tagName);
                        created.setCreatedAt(LocalDateTime.now());
                        return tagRepository.save(created);
                    });
            ProfileTag profileTag = new ProfileTag();
            profileTag.setProfile(profile);
            profileTag.setTag(tag);
            profileTags.add(profileTag);
        }
        profileTagRepository.saveAll(profileTags);
    }

    public ProfileResponse toResponse(InterviewProfile profile) {
        List<String> tags = profileTagRepository.findByProfileId(profile.getId()).stream()
                .map(profileTag -> profileTag.getTag().getName())
                .collect(Collectors.toList());
        List<interview.coach.api.dto.ProfileDtos.ProfileQuestionResponse> questions = profileQuestionRepository.findByProfileIdOrderByOrderIndexAsc(profile.getId()).stream()
                .map(this::toQuestionResponse)
                .toList();

        return new ProfileResponse(
                profile.getId(),
                profile.getTitle(),
                profile.getDescription(),
                profile.getDirection(),
                profile.getLevel(),
                profile.getStatus(),
                tags,
                questions,
                profile.getPublishedAt()
        );
    }

    private interview.coach.api.dto.ProfileDtos.ProfileQuestionResponse toQuestionResponse(ProfileQuestion profileQuestion) {
        return new interview.coach.api.dto.ProfileDtos.ProfileQuestionResponse(
                profileQuestion.getId(),
                profileQuestion.getQuestion().getId(),
                profileQuestion.getQuestion().getText(),
                profileQuestion.getQuestion().getQuestionType().name(),
                profileQuestion.getOrderIndex(),
                profileQuestion.isRequired()
        );
    }
}
