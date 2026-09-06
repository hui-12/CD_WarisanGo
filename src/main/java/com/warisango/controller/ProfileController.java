package com.warisango.controller;

import com.warisango.dto.ProfileUpdateRequest;
import com.warisango.dto.ProfileViewData;
import com.warisango.exception.ProfileUpdateException;
import com.warisango.service.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        addProfileModel(model, profileService.getProfileView(authentication.getName()));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String name,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String aboutMe,
                                RedirectAttributes redirectAttributes) {
        try {
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setDisplayName(name);
            request.setGender(gender);
            request.setAboutMe(aboutMe);
            profileService.updateProfile(authentication.getName(), request);
            redirectAttributes.addFlashAttribute("profileMessage", "Profile updated successfully.");
        } catch (ProfileUpdateException | IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("profileError", exception.getMessage());
        }
        return "redirect:/profile";
    }

    private void addProfileModel(Model model, ProfileViewData profile) {
        model.addAttribute("user", profile.user());
        model.addAttribute("memberSince", profile.memberSince());
        if ("admin".equalsIgnoreCase(profile.user().getRole())) {
            return;
        }
        model.addAttribute("calculatedTier", profile.calculatedTier());
        model.addAttribute("visits", profile.visits());
        model.addAttribute("visitCount", profile.visitCount());
        model.addAttribute("badgeCount", profile.badgeCount());
    }
}
