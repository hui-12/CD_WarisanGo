package com.warisango.controller;

import com.warisango.model.User;
import com.warisango.model.service.AuthService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the authenticated Firestore user available to every Thymeleaf page. */
@ControllerAdvice
public class CurrentUserModelAdvice {

    private final AuthService authService;

    public CurrentUserModelAdvice(AuthService authService) {
        this.authService = authService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authService.findUserByUid(authentication.getName()).orElse(null);
    }
}
