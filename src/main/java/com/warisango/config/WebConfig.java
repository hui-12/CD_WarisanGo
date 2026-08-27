package com.warisango.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDirectory = Path.of("uploads", "profile-images").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations(uploadDirectory.toUri().toString());
    }
}
