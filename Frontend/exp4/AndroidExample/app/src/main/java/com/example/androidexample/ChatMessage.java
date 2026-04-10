package com.example.androidexample;

/**
 * Model representing a single chat message.
 */
public class ChatMessage {

    private long id;
    private int senderId;
    private int receiverId;
    private String content;
    private String timestamp;
    private boolean sent;
    private String senderName; // for group chat

    public ChatMessage(String content, boolean sent) {
        this.content = content;
        this.sent = sent;
    }

    public ChatMessage(long id, int senderId, int receiverId, String content, String timestamp, boolean sent) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.sent = sent;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
}
