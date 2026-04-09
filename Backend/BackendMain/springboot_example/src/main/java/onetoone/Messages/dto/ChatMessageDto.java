package onetoone.Messages.dto;

public class ChatMessageDto {
    private Long conversationId;
    private Long senderUserId;
    private String content;

    public ChatMessageDto() {}

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}