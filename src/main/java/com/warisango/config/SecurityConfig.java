package com.warisango.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.access.AccessDeniedHandler;
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
            .csrf(csrf -> csrf.disable())
            .securityContext(securityContext -> securityContext
                .securityContextRepository(securityContextRepository)
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/heritage-gate",
                    "/directory",
                    "/business-directory",
                    "/business/**",
                    "/map",
                    "/interactive-map/**",
                    "/sse/businesses",
                    "/challenges",
                    "/api/approved",
                    "/api/stream",
                    "/api/auth/login",
                    "/api/auth/admin-login",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/challenges").permitAll()
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
                .requestMatchers(
                    "/ai-discovery",
                    "/st-discovery",
                    "/admin/**",
                    "/AdminChallengePage"
                ).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            if (request.getRequestURI().startsWith("/api/")) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "Admin privileges required.");
                return;
            }
            response.sendRedirect("/profile?error=admin");
        };
    }
}
