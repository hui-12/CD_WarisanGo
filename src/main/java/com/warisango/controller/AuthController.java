package com.warisango.controller;

import com.warisango.dto.LoginRequest;
import com.warisango.model.User;
import com.warisango.model.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        User authenticatedUser = authService.authenticateAndProcessUser(loginRequest);
        // Note: Real implementation would issue a JWT or Spring Session Cookie here.
        return ResponseEntity.ok(authenticatedUser);
    }
}