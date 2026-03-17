package interview.coach.repository;

import interview.coach.domain.DomainEnums.InterviewDirection;
import interview.coach.domain.DomainEnums.InterviewLevel;
import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.entity.InterviewProfile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterviewProfileRepository extends JpaRepository<InterviewProfile, UUID>, JpaSpecificationExecutor<InterviewProfile> {

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByStatusAndDirectionAndLevel(ProfileStatus status, InterviewDirection direction, InterviewLevel level);

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByStatusAndDirection(ProfileStatus status, InterviewDirection direction);

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByStatusAndLevel(ProfileStatus status, InterviewLevel level);

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByStatus(ProfileStatus status);

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByDirectionAndLevel(InterviewDirection direction, InterviewLevel level);

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByDirection(InterviewDirection direction);

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByLevel(InterviewLevel level);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findAll();
}
