package com.warisango.service;

import com.warisango.dto.ProfileUpdateRequest;
import com.warisango.exception.ProfileUpdateException;
import com.warisango.model.User;
import com.warisango.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
public class ProfileService {
    private static final Duration NAME_CHANGE_COOLDOWN = Duration.ofDays(30);
    private final UserRepository userRepository;
    private final ReviewPhotoStorageService photoStorageService;
    private static final long MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    public ProfileService(
            UserRepository userRepository,
            ReviewPhotoStorageService photoStorageService) {
        this.userRepository = userRepository;
        this.photoStorageService = photoStorageService;
    }

    public User updateProfile(String uid, ProfileUpdateRequest request) {
        if (request == null || request.getDisplayName() == null) {
            throw new ProfileUpdateException("displayName", "Display name is required.");
        }
        String requestedName = request.getDisplayName().trim().replaceAll("\\s+", " ");
        if (requestedName.length() < 3 || requestedName.length() > 30
                || !requestedName.matches("[A-Za-z0-9 ]+")) {
            throw new ProfileUpdateException(
                    "displayName",
                    "Display name must be 3 to 30 letters, numbers, or spaces."
            );
        }
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ProfileUpdateException("profile", "Your profile could not be found."));
        String name = requestedName;

        if (!name.equals(user.getName())) {
            long now = Instant.now().toEpochMilli();
            Long lastChanged = user.getLastNameChangeTimestamp();
            if (lastChanged != null) {
                long remaining = NAME_CHANGE_COOLDOWN.toMillis() - (now - lastChanged);
                if (remaining > 0) {
                    long days = Math.max(
                            1,
                            (long) Math.ceil(remaining / (double) Duration.ofDays(1).toMillis())
                    );
                    throw new ProfileUpdateException(
                            "displayName",
                            "Display name changes are limited to once every 30 days.",
                            days
                    );
                }
            }
            user.setName(name);
            user.setLastNameChangeTimestamp(now);
        }
        user.setGender(normalizeOptional(request.getGender()));
        user.setAboutMe(normalizeOptional(request.getAboutMe()));
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
            String avatarUrl = photoStorageService.storeAvatar(uid, file);
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

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
