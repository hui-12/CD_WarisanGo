package com.warisango.controller;

import com.warisango.dto.ProfileUpdateRequest;
import com.warisango.dto.UserResponse;
import com.warisango.exception.ProfileUpdateException;
import com.warisango.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileApiController {
    private final ProfileService profileService;
    public ProfileApiController(ProfileService profileService) { this.profileService = profileService; }

    @PatchMapping
    public ResponseEntity<UserResponse> update(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(UserResponse.from(
                profileService.updateProfile(authentication.getName(), request)));
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                             Authentication authentication) {
        String imageUrl = profileService.uploadAvatar(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("success", true, "imageUrl", imageUrl));
    }

    @ExceptionHandler(ProfileUpdateException.class)
    public ResponseEntity<Map<String, String>> handleProfileError(ProfileUpdateException exception) {
        return ResponseEntity.badRequest().body(Map.of("field", exception.getField(), "message", exception.getMessage(),
            "remainingDays", exception.getRemainingDays() == null ? "" : exception.getRemainingDays().toString()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        var error = exception.getBindingResult().getFieldError();
        return ResponseEntity.badRequest().body(Map.of("field", error == null ? "profile" : error.getField(),
            "message", error == null ? "Invalid profile details." : error.getDefaultMessage()));
    }
}
