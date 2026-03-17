package interview.coach.repository;

import interview.coach.domain.entity.SessionReport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionReportRepository extends JpaRepository<SessionReport, UUID> {

    @EntityGraph(attributePaths = "session")
    Optional<SessionReport> findBySessionId(UUID sessionId);
}
