package onetoone.Messages;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    private Long conversationId;

    private Long senderUserId;

    @Column(nullable = false)
    private String content;

    private LocalDateTime sentAt;

    private Boolean removed = false;
    private Long removedBy;
    private String removedReason;

    public Messages() {}

    // ===== GETTERS + SETTERS =====

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void setRemoved(boolean rm) {
        this.removed = rm;
    }

    public String setRemovedBy(Long mod){this.removedBy = mod;}
    public void setRemovedReason(String reason) {
        this.removedReason = reason;
    }

}