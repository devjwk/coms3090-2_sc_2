package onetoone.Conversations;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvoRepository extends JpaRepository<Conversation, Long> {
    // find all conversations a user is in (via join or custom query later)
}
