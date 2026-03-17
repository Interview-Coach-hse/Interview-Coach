package interview.coach.repository;

import interview.coach.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {"role", "preference"})
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = {"role", "preference"})
    Optional<User> findByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {"role", "preference"})
    List<User> findAll();
}
