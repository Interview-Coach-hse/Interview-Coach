package interview.coach.repository;

import interview.coach.domain.DomainEnums.ReportStatus;
import interview.coach.domain.DomainEnums.ScoreSource;
import interview.coach.domain.entity.SessionReport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionReportRepository extends JpaRepository<SessionReport, UUID> {

    @EntityGraph(attributePaths = "session")
    Optional<SessionReport> findBySessionId(UUID sessionId);

    long countByStatus(ReportStatus status);

    long countByStatusAndScoreSource(ReportStatus status, ScoreSource scoreSource);

    @Query(value = """
            select avg(extract(epoch from (generated_at - requested_at)))
            from session_reports
            where status = 'READY'
              and requested_at is not null
              and generated_at is not null
            """, nativeQuery = true)
    Double findAverageReadyReportGenerationSeconds();
}
