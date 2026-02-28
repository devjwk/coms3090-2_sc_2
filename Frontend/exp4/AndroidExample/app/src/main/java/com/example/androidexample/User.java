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
    private String major;
    private Integer age;

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
        this(userId, name, email, passwordHash, bio, hobbies, role, latitude, longitude, createdAt, isActive, null, null);
    }

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
                boolean isActive,
                String major,
                Integer age) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.bio = bio;
        this.hobbies = hobbies != null ? hobbies : new java.util.ArrayList<>();
        this.role = role;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.major = major != null ? major : "";
        this.age = age != null ? age : 0;
    }

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

    public void setName(String name) {

        this.name = name;
    }

    public void setBio(String bio) {

        this.bio = bio;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getMajor() {
        return major != null ? major : "";
    }

    public Integer getAge() {
        return age != null ? age : 0;
    }
}