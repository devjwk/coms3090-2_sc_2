package onetoone.Conversations;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;

    private String type; // DIRECT or GROUP
    private String name; // null for direct chat, group name for group chat

    private LocalDateTime createdAt;

    public Conversation() {}
}