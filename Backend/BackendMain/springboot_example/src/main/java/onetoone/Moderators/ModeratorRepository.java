package onetoone.Moderators;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ModeratorRepository extends JpaRepository<Moderator, Long> {
    Optional<Moderator> findByEmail(String email);
}