package com.warisango.model;

public class User {
    private String uid;
    private String email;
    private String name;
    private String avatar;
    private String role;
    private int totalPoints;
    private Long lastNameChangeTimestamp;

    public User() {} // Required for Firestore data mapping

    public User(String uid, String email, String name, String avatar, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.avatar = avatar;
        this.role = role;
        this.totalPoints = 0; // Default initialization
    }

    // --- Getters and Setters ---

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Long getLastNameChangeTimestamp() {
        return lastNameChangeTimestamp;
    }

    public void setLastNameChangeTimestamp(Long lastNameChangeTimestamp) {
        this.lastNameChangeTimestamp = lastNameChangeTimestamp;
    }
}
