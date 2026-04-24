package interview.coach.repository;

import interview.coach.domain.DomainEnums.MessageType;
import interview.coach.domain.DomainEnums.SenderType;
import interview.coach.domain.DomainEnums.SessionState;
import interview.coach.domain.entity.SessionMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionMessageRepository extends JpaRepository<SessionMessage, UUID> {

    Page<SessionMessage> findBySessionIdOrderBySequenceNumberAsc(UUID sessionId, Pageable pageable);

    List<SessionMessage> findBySessionIdOrderBySequenceNumberAsc(UUID sessionId);

    long countBySessionId(UUID sessionId);

    @Query("""
            select count(m)
            from SessionMessage m
            join m.session s
            where s.state = :sessionState
              and m.senderType = :senderType
              and m.messageType = :messageType
            """)
    long countBySessionStateAndSenderTypeAndMessageType(
            @Param("sessionState") SessionState sessionState,
            @Param("senderType") SenderType senderType,
            @Param("messageType") MessageType messageType
    );
}
