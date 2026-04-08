package onetoone.ConverstaionMembers;

import jakarta.persistence.*;

@Entity
@Table(name = "Conversation_Members")
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long conversationId;
    private Long userId;

    public ConversationMember() {}
}
