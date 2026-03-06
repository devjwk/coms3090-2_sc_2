package com.example.androidexample;

public class Match {

    private int matchId;
    private int user1Id;
    private int user2Id;
    private String status;
    private String createdAt;

    public Match(int matchId, int user1Id, int user2Id, String status, String createdAt) {
        this.matchId   = matchId;
        this.user1Id   = user1Id;
        this.user2Id   = user2Id;
        this.status    = status;
        this.createdAt = createdAt;
    }

    public int    getMatchId()   { return matchId; }
    public int    getUser1Id()   { return user1Id; }
    public int    getUser2Id()   { return user2Id; }
    public String getStatus()    { return status; }
    public String getCreatedAt() { return createdAt; }
}