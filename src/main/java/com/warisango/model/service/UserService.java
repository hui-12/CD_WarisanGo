package com.warisango.model.service;

import com.warisango.model.User;
import com.warisango.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Provides user display information to feature services without exposing
 * Firestore access to controllers or views.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getDisplayNameByUserId(String userId) {
        return userRepository.findDisplayNameByUserId(userId);
    }

    public Optional<User> getUserByUid(String uid) {
        return userRepository.findById(uid);
    }

    public void updateProfile(String uid, String name, String gender, String aboutMe) {
        if (name == null || name.isBlank() || name.trim().length() > 80) {
            throw new IllegalArgumentException("Name must contain between 1 and 80 characters.");
        }
        String normalizedGender = normalizeOptional(gender, 40);
        String normalizedAboutMe = normalizeOptional(aboutMe, 500);
        userRepository.updateProfile(uid, name.trim(), normalizedGender, normalizedAboutMe);
    }

    private String normalizeOptional(String value, int maximumLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException("Profile value is too long.");
        }
        return normalized;
    }
}
