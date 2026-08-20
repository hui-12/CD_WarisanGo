package com.warisango.model.service;

import com.warisango.model.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    public String getDisplayNameByTouristId(String touristId) {
        return userRepository.findDisplayNameByTouristId(touristId);
    }
}
