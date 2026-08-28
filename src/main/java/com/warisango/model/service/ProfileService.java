package com.warisango.model.service;

import com.warisango.dto.ProfileUpdateRequest;
import com.warisango.exception.ProfileUpdateException;
import com.warisango.model.User;
import com.warisango.model.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileService {
    private static final Duration NAME_CHANGE_COOLDOWN = Duration.ofDays(30);
    private final UserRepository userRepository;
    private static final long MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    public ProfileService(UserRepository userRepository) { this.userRepository = userRepository; }

    public User updateProfile(String uid, ProfileUpdateRequest request) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ProfileUpdateException("profile", "Your profile could not be found."));
        String name = request.getDisplayName().trim().replaceAll("\\s+", " ");

        if (!name.equals(user.getName())) {
            long now = Instant.now().toEpochMilli();
            Long lastChanged = user.getLastNameChangeTimestamp();
            if (lastChanged != null) {
                long remaining = NAME_CHANGE_COOLDOWN.toMillis() - (now - lastChanged);
                if (remaining > 0) {
                    long days = Math.max(1, (long) Math.ceil(remaining / (double) Duration.ofDays(1).toMillis()));
                    throw new ProfileUpdateException("displayName", "Display name changes are limited to once every 30 days.", days);
                }
            }
            user.setName(name);
            user.setLastNameChangeTimestamp(now);
        }
        userRepository.save(user);
        return user;
    }

    public String uploadAvatar(String uid, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProfileUpdateException("avatarUrl", "Please select an image to upload.");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = extensionOf(originalFilename);
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ProfileUpdateException("avatarUrl", "Use a JPG, PNG, or WebP image no larger than 5 MB.");
        }
        try {
            Path directory = Path.of("uploads", "profile-images").toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String filename = uid + "-" + UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            String avatarUrl = "/uploads/profile-images/" + filename;
            User user = userRepository.findById(uid)
                    .orElseThrow(() -> new ProfileUpdateException("profile", "Your profile could not be found."));
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            return avatarUrl;
        } catch (ProfileUpdateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ProfileUpdateException("avatarUrl", "Image upload failed. Please try again.");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int extensionIndex = filename.lastIndexOf('.');
        return extensionIndex < 0 ? "" : filename.substring(extensionIndex).toLowerCase();
    }

}
