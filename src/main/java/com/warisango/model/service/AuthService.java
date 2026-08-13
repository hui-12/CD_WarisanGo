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
            // Verify the integrity of the token retrieved from JS using Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(loginRequest.getIdToken());
            String uid = decodedToken.getUid();

            Optional<User> existingUser = userRepository.findById(uid);

            if (existingUser.isPresent()) {
                logger.info("Returning user {} successfully authenticated.", uid);
                return existingUser.get();
            } else {
                logger.info("Registering new user {} in Firestore.", uid);
                User newUser = new User(
                    uid,
                    decodedToken.getEmail(),
                    decodedToken.getName(),
                    decodedToken.getPicture(),
                    loginRequest.getRole()
                );
                userRepository.save(newUser);
                return newUser;
            }
        } catch (FirebaseAuthException e) {
            logger.error("Invalid Firebase ID token provided.", e);
            throw new IllegalArgumentException("Authentication failed due to invalid token.");
        }
    }
}