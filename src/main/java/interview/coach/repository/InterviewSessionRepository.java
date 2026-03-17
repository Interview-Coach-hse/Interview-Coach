package interview.coach.repository;

import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.InterviewSession;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID>, JpaSpecificationExecutor<InterviewSession> {

    @EntityGraph(attributePaths = {"profile", "user"})
    Optional<InterviewSession> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndStateIn(UUID userId, Collection<SessionState> states);

    @EntityGraph(attributePaths = {"profile"})
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
