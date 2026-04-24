package interview.coach.repository;

import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.InterviewSession;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID>, JpaSpecificationExecutor<InterviewSession> {

    @EntityGraph(attributePaths = {"profile", "user"})
    Optional<InterviewSession> findByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from InterviewSession s
            join fetch s.profile p
            where s.id = :id
            """)
    Optional<InterviewSession> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByUserIdAndStateIn(UUID userId, Collection<SessionState> states);

    long countByState(SessionState state);

    long countByStateIn(Collection<SessionState> states);

    @Query("select count(distinct s.user.id) from InterviewSession s where s.state in :states")
    long countDistinctUsersByStateIn(@Param("states") Collection<SessionState> states);

    @EntityGraph(attributePaths = {"profile"})
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
