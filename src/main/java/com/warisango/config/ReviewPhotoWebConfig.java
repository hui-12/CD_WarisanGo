package com.warisango.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves locally stored review photos through their ReviewPhotos.photoUrl values.
 */
@Configuration
public class ReviewPhotoWebConfig implements WebMvcConfigurer {

    private final Path uploadDirectory;

    public ReviewPhotoWebConfig(
            @Value("${warisango.review.upload-directory:uploads/reviews}") String uploadDirectory) {
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = uploadDirectory.toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/uploads/reviews/**")
                .addResourceLocations(resourceLocation);
    }
}
