package com.warisango.controller;

import com.warisango.model.User;
import com.warisango.model.service.AuthService;
import com.warisango.model.service.UserService;
import com.warisango.model.service.VisitService;
import com.warisango.model.service.BadgeService;
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
    private final UserService userService;
    private final VisitService visitService;
    private final BadgeService badgeService;

    public ProfileController(AuthService authService, UserService userService,
                             VisitService visitService, BadgeService badgeService) {
        this.authService = authService;
        this.userService = userService;
        this.visitService = visitService;
        this.badgeService = badgeService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String touristId = authentication.getName();
        authService.findUserByUid(touristId).ifPresent(user -> addProfileModel(model, user, touristId));
        return "ProfilePage";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String name,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String aboutMe,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(authentication.getName(), name, gender, aboutMe);
            redirectAttributes.addFlashAttribute("profileMessage", "Profile updated successfully.");
        } catch (IllegalArgumentException exception) {
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
        var visits = visitService.getVisits(touristId);
        model.addAttribute("calculatedTier", TierCalculator.tierFor(user.getTotalPoints()));
        model.addAttribute("visits", visits);
        model.addAttribute("visitCount", visits.size());
        model.addAttribute("badgeCount", badgeService.getSummary(touristId).earned());
    }
}
