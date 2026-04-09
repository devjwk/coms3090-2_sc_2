package onetoone.ConverstaionMembers;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConvoMemRepository extends JpaRepository<ConversationMember, Long> {
    List<ConversationMember> findByConversationId(Long conversationId);
    List<ConversationMember> findByUserId(Long userId);
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}