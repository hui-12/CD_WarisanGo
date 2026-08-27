package com.warisango.model.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.warisango.dto.LoginRequest;
import com.warisango.model.User;
import com.warisango.model.repository.UserRepository;
import com.warisango.model.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public AuthService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    public User authenticateAndProcessUser(LoginRequest loginRequest) {
        try {
            // Verify token with Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(loginRequest.getIdToken());
            String uid = decodedToken.getUid();

            Optional<User> existingUser = userRepository.findById(uid);

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                String role = isAdmin(user, uid) ? "ADMIN" : "TOURIST";
                if (!role.equals(user.getRole())) {
                    user.setRole(role);
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
                    adminRepository.existsByUserId(uid) ? "ADMIN" : "TOURIST"
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

    private boolean isAdmin(User user, String uid) {
        return "ADMIN".equals(normalizeRole(user.getRole())) || adminRepository.existsByUserId(uid);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase();
    }

}
