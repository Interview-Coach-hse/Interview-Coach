package interview.coach.repository;

import interview.coach.domain.entity.Question;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface QuestionRepository extends JpaRepository<Question, UUID>, JpaSpecificationExecutor<Question> {

    @EntityGraph(attributePaths = "createdBy")
    java.util.Optional<Question> findFirstByTextIgnoreCase(String text);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    Page<Question> findAll(Specification<Question> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "createdBy")
    java.util.Optional<Question> findById(UUID id);
}
