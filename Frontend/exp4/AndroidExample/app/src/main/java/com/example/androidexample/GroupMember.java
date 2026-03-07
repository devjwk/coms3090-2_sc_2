package com.example.androidexample;

/**
 * Data model for a Group Member.
 * Matches the backend GMController.
 */
public class GroupMember {
    private long id;
    private long userId;
    private long groupId;
    private String status; // e.g., "active", "pending", "banned"
    private boolean is_moderator;

    public GroupMember(long id, long userId, long groupId, String status, boolean is_moderator) {
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
        this.status = status;
        this.is_moderator = is_moderator;
    }

    // Getters
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public long getGroupId() { return groupId; }
    public String getStatus() { return status; }
    public boolean isModerator() { return is_moderator; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setModerator(boolean is_moderator) { this.is_moderator = is_moderator; }
}
