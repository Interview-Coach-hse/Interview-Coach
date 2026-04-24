package interview.coach.repository;

import interview.coach.domain.DomainEnums.ProfileStatus;
import interview.coach.domain.entity.InterviewProfile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterviewProfileRepository extends JpaRepository<InterviewProfile, UUID>, JpaSpecificationExecutor<InterviewProfile> {

    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findByStatus(ProfileStatus status);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    List<InterviewProfile> findAll();
}
