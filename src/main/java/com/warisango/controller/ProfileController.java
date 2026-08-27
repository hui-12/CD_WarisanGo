package com.warisango.controller;

import com.warisango.model.service.AuthService;
import com.warisango.model.repository.PointsHistoryRepository;
import com.warisango.model.repository.VisitHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final AuthService authService;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final VisitHistoryRepository visitHistoryRepository;
    public ProfileController(AuthService authService, PointsHistoryRepository pointsHistoryRepository, VisitHistoryRepository visitHistoryRepository) {
        this.authService = authService;
        this.pointsHistoryRepository = pointsHistoryRepository;
        this.visitHistoryRepository = visitHistoryRepository;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        authService.findUserByUid(authentication.getName()).ifPresent(user -> model.addAttribute("user", user));
        model.addAttribute("pointsHistory", pointsHistoryRepository.findByUserId(authentication.getName()));
        model.addAttribute("visitHistory", visitHistoryRepository.findByUserId(authentication.getName()));
        return "ProfilePage";
    }
}
