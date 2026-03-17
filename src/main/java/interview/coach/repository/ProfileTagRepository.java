package interview.coach.repository;

import interview.coach.domain.entity.ProfileTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileTagRepository extends JpaRepository<ProfileTag, UUID> {

    @EntityGraph(attributePaths = "tag")
    List<ProfileTag> findByProfileId(UUID profileId);

    void deleteByProfileId(UUID profileId);
}
