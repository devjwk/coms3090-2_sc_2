package com.example.androidexample;

/**
 * Data model for a Match between two users.
 */
public class Match {
    private long matchId;
    private long user1Id;
    private long user2Id;
    private String status; // e.g., "pending", "accepted", "rejected"
    private String createdAt;

    public Match(long matchId, long user1Id, long user2Id, String status, String createdAt) {
        this.matchId = matchId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public long getMatchId() { return matchId; }
    public long getUser1Id() { return user1Id; }
    public long getUser2Id() { return user2Id; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}
