package com.warisango.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String userId;
    private String email;
    private String name;
    private String avatar;
    private String role;
    private int totalPoints;
    private Long lastNameChangeTimestamp;
    private String tierStatus;
    private String gender;
    private String aboutMe;
    private Timestamp createdAt;

    public User() {
        // Required for Firestore data mapping.
    }

    public User(String userId, String email, String name, String avatar, String role) {
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getTierStatus() {
        return tierStatus;
    }

    public void setTierStatus(String tierStatus) {
        this.tierStatus = tierStatus == null ? null : tierStatus.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
