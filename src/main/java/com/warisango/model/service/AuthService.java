package com.warisango.model.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.warisango.dto.LoginRequest;
import com.warisango.model.User;
import com.warisango.model.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticateAndProcessUser(LoginRequest loginRequest) {
        try {
            // Verify token with Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(loginRequest.getIdToken());
            String uid = decodedToken.getUid();
            String assignedRole = resolveRole(decodedToken);

            Optional<User> existingUser = userRepository.findById(uid);

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (!assignedRole.equals(user.getRole())) {
                    user.setRole(assignedRole);
                    userRepository.save(user);
                }
                logger.info("Returning user {} successfully authenticated.", uid);
                return user;
            } else {
                logger.info("Registering new user {} in Firestore.", uid);
                User newUser = new User(
                    uid,
                    decodedToken.getEmail(),
                    decodedToken.getName(),
                    decodedToken.getPicture(),
                    assignedRole
                );
                userRepository.save(newUser);
                return newUser;
            }
        } catch (FirebaseAuthException e) {
            // Log the exact cause from Firebase SDK
            logger.error("Firebase token verification failed. Error code: {}, Message: {}",
                e.getAuthErrorCode(), e.getMessage(), e);
            throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        }
    }

    public Optional<User> findUserByUid(String uid) {
        return userRepository.findById(uid);
    }

    private String resolveRole(FirebaseToken firebaseToken) {
        Object adminClaim = firebaseToken.getClaims().get("admin");
        return Boolean.TRUE.equals(adminClaim) ? "admin" : "tourist";
    }
}
