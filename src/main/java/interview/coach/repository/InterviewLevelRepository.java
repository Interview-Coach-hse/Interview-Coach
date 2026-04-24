package interview.coach.repository;

import interview.coach.domain.entity.InterviewLevelRef;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewLevelRepository extends JpaRepository<InterviewLevelRef, UUID> {

    Optional<InterviewLevelRef> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
