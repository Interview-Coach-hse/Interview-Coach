package interview.coach.repository;

import interview.coach.domain.entity.InterviewDirectionRef;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewDirectionRepository extends JpaRepository<InterviewDirectionRef, UUID> {

    Optional<InterviewDirectionRef> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
