package onetoone.Messages.dto;

import java.time.LocalDateTime;

public class ChatMessageResponseDto {

    private Long messageId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private Boolean removed;

    public ChatMessageResponseDto() {
    }

    public ChatMessageResponseDto(Long messageId,
                                  String senderName,
                                  String content,
                                  LocalDateTime timestamp,
                                  Boolean removed) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.removed = removed;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }
}