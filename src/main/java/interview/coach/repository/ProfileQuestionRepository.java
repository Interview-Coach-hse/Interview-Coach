package interview.coach.repository;

import interview.coach.domain.entity.ProfileQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileQuestionRepository extends JpaRepository<ProfileQuestion, UUID> {

    @EntityGraph(attributePaths = "question")
    List<ProfileQuestion> findByProfileIdOrderByOrderIndexAsc(UUID profileId);

    @EntityGraph(attributePaths = "question")
    List<ProfileQuestion> findByProfileIdInOrderByProfileIdAscOrderIndexAsc(List<UUID> profileIds);

    boolean existsByProfileIdAndOrderIndex(UUID profileId, int orderIndex);

    boolean existsByProfileIdAndOrderIndexAndIdNot(UUID profileId, int orderIndex, UUID id);

    boolean existsByProfileIdAndQuestionId(UUID profileId, UUID questionId);

    @EntityGraph(attributePaths = "question")
    java.util.Optional<ProfileQuestion> findByIdAndProfileId(UUID id, UUID profileId);

    void deleteByProfileId(UUID profileId);
}
