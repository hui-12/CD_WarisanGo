package com.warisango.controller;

import com.warisango.model.User;
import com.warisango.dto.ProfileUpdateRequest;
import com.warisango.exception.ProfileUpdateException;
import com.warisango.service.AuthService;
import com.warisango.service.ProfileService;
import com.warisango.service.VisitService;
import com.warisango.service.BadgeService;
import com.warisango.util.TierCalculator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class ProfileController {

    private final AuthService authService;
    private final ProfileService profileService;
    private final VisitService visitService;
    private final BadgeService badgeService;

    public ProfileController(AuthService authService, ProfileService profileService,
                             VisitService visitService, BadgeService badgeService) {
        this.authService = authService;
        this.profileService = profileService;
        this.visitService = visitService;
        this.badgeService = badgeService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String touristId = authentication.getName();
        authService.findUserByUid(touristId).ifPresent(user -> addProfileModel(model, user, touristId));
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

    private void addProfileModel(Model model, User user, String touristId) {
        model.addAttribute("user", user);
        String memberSince = "Not available";
        if (user.getCreatedAt() != null) {
            memberSince = DateTimeFormatter.ofPattern("d MMMM uuuu")
                    .withZone(ZoneId.of("Asia/Kuala_Lumpur"))
                    .format(user.getCreatedAt().toDate().toInstant());
        }
        model.addAttribute("memberSince", memberSince);
        if ("admin".equalsIgnoreCase(user.getRole())) {
            return;
        }
        var visits = visitService.getVisits(touristId);
        model.addAttribute("calculatedTier", TierCalculator.tierFor(user.getTotalPoints()));
        model.addAttribute("visits", visits);
        model.addAttribute("visitCount", visits.size());
        model.addAttribute("badgeCount", badgeService.getSummary(touristId).earned());
    }
}
