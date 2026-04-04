package com.example.androidexample;

/**
 * Represents a group member in the application.
 * This model corresponds to the backend GMController and stores
 * information about a user's membership within a group.
 */
public class GroupMember {
    /** Unique identifier for the group member record */
    private long id;
    /** ID of the user associated with this membership */
    private long userId;
    /** ID of the group the user belongs to */
    private long groupId;
    /**
     * Current membership status.
     * Possible values include "active", "pending", or "banned".
     */
    private String status; // e.g., "active", "pending", "banned"
    /** Indicates whether the user is a moderator in the group */
    private boolean is_moderator;

    /**
     * Constructs a GroupMember object with all required fields.
     *
     * @param id unique identifier for the group member
     * @param userId ID of the user
     * @param groupId ID of the group
     * @param status current membership status
     * @param is_moderator true if the user is a moderator, false otherwise
     */
    public GroupMember(long id, long userId, long groupId, String status, boolean is_moderator) {
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
        this.status = status;
        this.is_moderator = is_moderator;
    }
    /**
     * Returns the unique identifier of the group member.
     *
     * @return the group member ID
     */
    // Getters
    public long getId() { return id; }
    /**
     * Returns the user ID associated with this membership.
     *
     * @return the user ID
     */
    public long getUserId() { return userId; }
    /**
     * Returns the group ID associated with this membership.
     *
     * @return the group ID
     */
    public long getGroupId() { return groupId; }
    /**
     * Returns the current membership status.
     *
     * @return the membership status (e.g., "active", "pending", "banned")
     */
    public String getStatus() { return status; }
    /**
     * Indicates whether the user is a moderator.
     *
     * @return true if the user is a moderator, false otherwise
     */
    public boolean isModerator() { return is_moderator; }
    /**
     * Updates the membership status.
     *
     * @param status the new status to set
     */
    // Setters
    public void setStatus(String status) { this.status = status; }
    /**
     * Sets the moderator flag for the group member.
     *
     * @param is_moderator true to assign moderator role, false to remove it
     */
    public void setModerator(boolean is_moderator) { this.is_moderator = is_moderator; }
}
