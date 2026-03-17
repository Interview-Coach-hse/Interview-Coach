package interview.coach.repository;

import interview.coach.domain.entity.Question;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Override
    @EntityGraph(attributePaths = "createdBy")
    List<Question> findAll();

    @Override
    @EntityGraph(attributePaths = "createdBy")
    java.util.Optional<Question> findById(UUID id);
}
