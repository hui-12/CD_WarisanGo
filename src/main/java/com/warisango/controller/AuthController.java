package com.warisango.controller;

import com.warisango.dto.LoginRequest;
import com.warisango.dto.UserResponse;
import com.warisango.model.User;
import com.warisango.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService, SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        User authenticatedUser = authService.authenticateTourist(loginRequest);

        saveAuthenticatedUser(authenticatedUser, request, response);
        return ResponseEntity.ok(UserResponse.from(authenticatedUser));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<UserResponse> loginAdmin(@Valid @RequestBody LoginRequest loginRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        User authenticatedUser = authService.authenticateAdmin(loginRequest);

        saveAuthenticatedUser(authenticatedUser, request, response);
        return ResponseEntity.ok(UserResponse.from(authenticatedUser));
    }

    private void saveAuthenticatedUser(User authenticatedUser,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
            authenticatedUser.getUserId(),
            null,
            AuthorityUtils.createAuthorityList("ROLE_" + authenticatedUser.getRole().toUpperCase())
        );
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
        request.getSession(true).setAttribute("navbarUser", authenticatedUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
