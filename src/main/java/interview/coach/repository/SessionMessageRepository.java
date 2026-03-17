package interview.coach.repository;

import interview.coach.domain.entity.SessionMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionMessageRepository extends JpaRepository<SessionMessage, UUID> {

    Page<SessionMessage> findBySessionIdOrderBySequenceNumberAsc(UUID sessionId, Pageable pageable);

    long countBySessionId(UUID sessionId);
}
