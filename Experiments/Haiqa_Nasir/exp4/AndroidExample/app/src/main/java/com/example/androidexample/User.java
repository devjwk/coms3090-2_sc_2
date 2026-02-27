package com.example.androidexample;

import java.util.List;

public class User {

    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String bio;
    private List<String> hobbies;
    private String role;
    private double latitude;
    private double longitude;
    private String createdAt;
    private boolean isActive;

    // ✅ Constructor (this is what your activity calls)
    public User(int userId,
                String name,
                String email,
                String passwordHash,
                String bio,
                List<String> hobbies,
                String role,
                double latitude,
                double longitude,
                String createdAt,
                boolean isActive) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.bio = bio;
        this.hobbies = hobbies;
        this.role = role;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // ✅ Getters (this is what msgResponse uses)
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public String getRole() {
        return role;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return isActive;
    }
}