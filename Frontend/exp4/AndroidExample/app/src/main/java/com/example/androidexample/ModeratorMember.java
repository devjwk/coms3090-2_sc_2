package com.example.androidexample;

public class ModeratorMember {
    private int userId;
    private String displayName;

    public ModeratorMember(int userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public int getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
