package com.warisango.model.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.warisango.dto.LoginRequest;
import com.warisango.exception.RoleAccessDeniedException;
import com.warisango.model.User;
import com.warisango.model.repository.AdminRepository;
import com.warisango.model.repository.UserRepository;
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

    public User authenticateTourist(LoginRequest loginRequest) {
        FirebaseToken decodedToken = verifyToken(loginRequest);
        if (adminRepository.existsByUserId(decodedToken.getUid())) {
            throw new RoleAccessDeniedException("Admin accounts must use the admin login page.");
        }
        return createOrUpdateUser(decodedToken, "tourist");
    }

    public User authenticateAdmin(LoginRequest loginRequest) {
        FirebaseToken decodedToken = verifyToken(loginRequest);
        if (!adminRepository.existsByUserId(decodedToken.getUid())) {
            throw new RoleAccessDeniedException("This account is not registered as an administrator.");
        }
        return createOrUpdateUser(decodedToken, "admin");
    }

    private FirebaseToken verifyToken(LoginRequest loginRequest) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(loginRequest.getIdToken());
        } catch (FirebaseAuthException e) {
            logger.error("Firebase token verification failed. Error code: {}, Message: {}",
                e.getAuthErrorCode(), e.getMessage(), e);
            throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        }
    }

    private User createOrUpdateUser(FirebaseToken decodedToken, String assignedRole) {
        String uid = decodedToken.getUid();
        Optional<User> existingUser = userRepository.findById(uid);

        if (existingUser.isPresent()) {
            User user = userRepository.initializeMissingProfileFields(existingUser.get());
            boolean identityChanged = synchronizeGoogleIdentity(user, decodedToken);
            if (!assignedRole.equalsIgnoreCase(user.getRole())) {
                user.setRole(assignedRole);
                identityChanged = true;
            }
            if (identityChanged) {
                userRepository.save(user);
            }
            logger.info("User {} authenticated with role {}.", uid, assignedRole);
            return user;
        }

        logger.info("Registering new user {} in Firestore with role {}.", uid, assignedRole);
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

    private boolean synchronizeGoogleIdentity(User user, FirebaseToken decodedToken) {
        boolean changed = false;
        if (hasText(decodedToken.getEmail()) && !decodedToken.getEmail().equals(user.getEmail())) {
            user.setEmail(decodedToken.getEmail());
            changed = true;
        }
        if (hasText(decodedToken.getName()) && !decodedToken.getName().equals(user.getName())) {
            user.setName(decodedToken.getName());
            changed = true;
        }
        if (hasText(decodedToken.getPicture()) && !decodedToken.getPicture().equals(user.getAvatar())) {
            user.setAvatar(decodedToken.getPicture());
            changed = true;
        }
        return changed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public Optional<User> findUserByUid(String uid) {
        return userRepository.findById(uid);
    }

}
