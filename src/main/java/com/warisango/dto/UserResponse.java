package com.warisango.dto;

import com.google.cloud.Timestamp;
import com.warisango.model.User;

/**
 * Public user representation returned by authentication and profile APIs.
 */
public record UserResponse(
        String userId,
        String email,
        String name,
        String avatar,
        String role,
        int totalPoints,
        Long lastNameChangeTimestamp,
        String tierStatus,
        String gender,
        String aboutMe,
        Timestamp createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getAvatar(),
                user.getRole(),
                user.getTotalPoints(),
                user.getLastNameChangeTimestamp(),
                user.getTierStatus(),
                user.getGender(),
                user.getAboutMe(),
                user.getCreatedAt());
    }
}
