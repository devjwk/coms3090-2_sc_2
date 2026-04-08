package onetoone.Matches;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
    List<Match> findByUser2IdAndStatus(Long user2Id, MatchStatus status);
    List<Match> findByUser1IdAndStatus(Long user1Id, MatchStatus status);
}