package com.warisango.model;

import com.google.cloud.Timestamp;

public class User {
    private String uid;
    private String email;
    private String name;
    private String avatar;
    private String role;
    private int totalPoints;
    private String tierStatus;
    private String gender;
    private String aboutMe;
    private Timestamp createdAt;

    public User() {} // Required for Firestore data mapping

    public User(String uid, String email, String name, String avatar, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.avatar = avatar;
        this.role = role;
        this.totalPoints = 0;
        this.tierStatus = "Bronze";
        this.gender = null;
        this.aboutMe = null;
        this.createdAt = Timestamp.now();
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

    public String getTierStatus() {
        return tierStatus;
    }

    public void setTierStatus(String tierStatus) {
        this.tierStatus = tierStatus == null ? null : tierStatus.trim();
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
