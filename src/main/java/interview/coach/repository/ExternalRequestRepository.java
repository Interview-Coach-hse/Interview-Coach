package interview.coach.repository;

import interview.coach.domain.entity.ExternalRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalRequestRepository extends JpaRepository<ExternalRequest, UUID> {

    List<ExternalRequest> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
