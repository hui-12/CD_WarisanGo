package com.warisango.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                          SecurityContextRepository securityContextRepository) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/api/**",
                    "/reviews/moderation/check",
                    "/reviews/like/**",
                    "/reviews/comments/like/**"
            ))
            .securityContext(securityContext -> securityContext
                .securityContextRepository(securityContextRepository)
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .defaultAuthenticationEntryPointFor(
                        apiAuthenticationEntryPoint(),
                        request -> request.getRequestURI().startsWith("/api/")
                )
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/heritage-gate",
                    "/terms-of-service",
                    "/privacy-policy",
                    "/how-it-works",
                    "/actuator/health",
                    "/directory",
                    "/business-directory",
                    "/business/**",
                    "/map",
                    "/interactive-map/**",
                    "/sse/businesses",
                    "/challenges",
                    "/api/auth/login",
                    "/api/auth/admin-login",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/challenges").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/map/**").permitAll()
                .requestMatchers(
                    "/api/admin/**",
                    "/ai-discovery",
                    "/st-discovery",
                    "/admin/**",
                    "/AdminChallengePage"
                ).hasRole("ADMIN")
                .requestMatchers(
                    "/profile/**",
                    "/api/checkin",
                    "/api/checkin/**",
                    "/api/save/**",
                    "/api/saved-listings/**",
                    "/api/challenges/join/**",
                    "/api/challenges/*/join",
                    "/api/challenges/*/claim"
                ).authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, exception) -> writeJsonError(
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication required."
        );
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            if (request.getRequestURI().startsWith("/api/")) {
                writeJsonError(response, HttpStatus.FORBIDDEN, "Access denied.");
                return;
            }
            response.sendRedirect("/profile?error=admin");
        };
    }

    private static void writeJsonError(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
